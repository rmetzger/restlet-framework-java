/**
 * Copyright 2005-2009 Noelios Technologies.
 * 
 * The contents of this file are subject to the terms of one of the following
 * open source licenses: LGPL 3.0 or LGPL 2.1 or CDDL 1.0 or EPL 1.0 (the
 * "Licenses"). You can select the license that you prefer but you may not use
 * this file except in compliance with one of these Licenses.
 * 
 * You can obtain a copy of the LGPL 3.0 license at
 * http://www.opensource.org/licenses/lgpl-3.0.html
 * 
 * You can obtain a copy of the LGPL 2.1 license at
 * http://www.opensource.org/licenses/lgpl-2.1.php
 * 
 * You can obtain a copy of the CDDL 1.0 license at
 * http://www.opensource.org/licenses/cddl1.php
 * 
 * You can obtain a copy of the EPL 1.0 license at
 * http://www.opensource.org/licenses/eclipse-1.0.php
 * 
 * See the Licenses for the specific language governing permissions and
 * limitations under the Licenses.
 * 
 * Alternatively, you can obtain a royalty free commercial license with less
 * limitations, transferable or non-transferable, directly at
 * http://www.noelios.com/products/restlet-engine
 * 
 * Restlet is a registered trademark of Noelios Technologies.
 */

package org.restlet.ext.rdf.internal.n3;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.restlet.data.Reference;
import org.restlet.ext.rdf.Graph;
import org.restlet.ext.rdf.Literal;
import org.restlet.ext.rdf.internal.RdfConstants;
import org.restlet.ext.rdf.internal.turtle.BlankNodeToken;
import org.restlet.ext.rdf.internal.turtle.Context;
import org.restlet.ext.rdf.internal.turtle.LexicalUnit;
import org.restlet.ext.rdf.internal.turtle.ListToken;
import org.restlet.ext.rdf.internal.turtle.RdfTurtleParsingContentHandler;
import org.restlet.ext.rdf.internal.turtle.StringToken;
import org.restlet.ext.rdf.internal.turtle.Token;
import org.restlet.ext.rdf.internal.turtle.UriToken;
import org.restlet.representation.Representation;

/**
 * Handler of RDF content according to the N3 notation.
 * 
 * @author Thierry Boileau
 */
public class RdfN3ParsingContentHandler extends RdfTurtleParsingContentHandler {

    /**
     * Constructor.
     * 
     * @param linkSet
     *            The set of links to update during the parsing.
     * @param rdfN3Representation
     *            The representation to read.
     * @throws IOException
     */
    public RdfN3ParsingContentHandler(Graph linkSet,
            Representation rdfN3Representation) throws IOException {
        super(linkSet, rdfN3Representation);
    }

    @Override
    protected void generateLinks(List<LexicalUnit> lexicalUnits) {
        Object currentSubject = null;
        Reference currentPredicate = null;
        Object currentObject = null;
        int nbTokens = 0;
        boolean swapSubjectObject = false;
        for (int i = 0; i < lexicalUnits.size(); i++) {
            LexicalUnit lexicalUnit = lexicalUnits.get(i);

            nbTokens++;
            switch (nbTokens) {
            case 1:
                if (",".equals(lexicalUnit.getValue())) {
                    nbTokens++;
                } else if (!";".equals(lexicalUnit.getValue())) {
                    currentSubject = lexicalUnit.resolve();
                }
                break;
            case 2:
                if ("is".equalsIgnoreCase(lexicalUnit.getValue())) {
                    nbTokens--;
                    swapSubjectObject = true;
                } else if ("has".equalsIgnoreCase(lexicalUnit.getValue())) {
                    nbTokens--;
                } else if ("=".equalsIgnoreCase(lexicalUnit.getValue())) {
                    currentPredicate = RdfConstants.PREDICATE_SAME;
                } else if ("=>".equalsIgnoreCase(lexicalUnit.getValue())) {
                    currentPredicate = RdfConstants.PREDICATE_IMPLIES;
                } else if ("<=".equalsIgnoreCase(lexicalUnit.getValue())) {
                    swapSubjectObject = true;
                    currentPredicate = RdfConstants.PREDICATE_IMPLIES;
                } else if ("a".equalsIgnoreCase(lexicalUnit.getValue())) {
                    currentPredicate = RdfConstants.PREDICATE_TYPE;
                } else if ("!".equalsIgnoreCase(lexicalUnit.getValue())) {
                    currentObject = new BlankNodeToken(newBlankNodeId())
                            .resolve();
                    currentPredicate = getPredicate(lexicalUnits.get(++i));
                    this.link(currentSubject, currentPredicate, currentObject);
                    currentSubject = currentObject;
                    nbTokens = 1;
                } else if ("^".equalsIgnoreCase(lexicalUnit.getValue())) {
                    currentObject = currentSubject;
                    currentPredicate = getPredicate(lexicalUnits.get(++i));
                    currentSubject = new BlankNodeToken(newBlankNodeId())
                            .resolve();
                    this.link(currentSubject, currentPredicate, currentObject);
                    nbTokens = 1;
                } else {
                    currentPredicate = getPredicate(lexicalUnit);
                }
                break;
            case 3:
                if ("of".equalsIgnoreCase(lexicalUnit.getValue())) {
                    nbTokens--;
                } else {
                    if (swapSubjectObject) {
                        currentObject = currentSubject;
                        currentSubject = lexicalUnit.resolve();
                    } else {
                        currentObject = lexicalUnit.resolve();
                    }
                    this.link(currentSubject, currentPredicate, currentObject);
                    nbTokens = 0;
                    swapSubjectObject = false;
                }
                break;
            default:
                break;
            }
        }
    }

    /**
     * Returns the given lexical unit as a predicate.
     * 
     * @param lexicalUnit
     *            The lexical unit to get as a predicate.
     * @return A RDF URI reference of the predicate.
     */
    private Reference getPredicate(LexicalUnit lexicalUnit) {
        Reference result = null;
        Object p = lexicalUnit.resolve();
        if (p instanceof Reference) {
            result = (Reference) p;
        } else if (p instanceof String) {
            result = new Reference((String) p);
        }

        return result;
    }

    @Override
    protected boolean isDelimiter(int c) {
        return isWhiteSpace(c) || c == '^' || c == '!' || c == '=' || c == '<'
                || c == '"' || c == '{' || c == '}' || c == '[' || c == ']'
                || c == '(' || c == ')' || c == '.' || c == ';' || c == ','
                || c == '@';
    }

    @Override
    public void link(Graph source, Reference typeRef, Literal target) {
        getLinkSet().add(source, typeRef, target);
    }

    @Override
    public void link(Graph source, Reference typeRef, Reference target) {
        getLinkSet().add(source, typeRef, target);
    }

    /**
     * Callback method used when a link is parsed or written.
     * 
     * @param source
     *            The source or subject of the link.
     * @param typeRef
     *            The type reference of the link.
     * @param target
     *            The target or object of the link.
     */
    private void link(Object source, Reference typeRef, Object target) {
        if (source instanceof Reference) {
            if (target instanceof Reference) {
                link((Reference) source, typeRef, (Reference) target);
            } else if (target instanceof Literal) {
                link((Reference) source, typeRef, (Literal) target);
            } else {
                org.restlet.Context
                        .getCurrentLogger()
                        .warning(
                                "The N3 document contains an object which is neither a Reference nor a literal.");
            }
        } else if (source instanceof Graph) {
            if (target instanceof Reference) {
                link((Graph) source, typeRef, (Reference) target);
            } else if (target instanceof Literal) {
                link((Graph) source, typeRef, (Literal) target);
            } else {
                org.restlet.Context
                        .getCurrentLogger()
                        .warning(
                                "The N3 document contains an object which is neither a Reference nor a literal.");
            }
        }
    }

    @Override
    public void link(Reference source, Reference typeRef, Literal target) {
        super.link(source, typeRef, target);
    }

    @Override
    public void link(Reference source, Reference typeRef, Reference target) {
        super.link(source, typeRef, target);
    }

    @Override
    protected void parseBlankNode(BlankNodeToken blankNode) throws IOException {
        step();
        do {
            consumeWhiteSpaces();
            switch (getChar()) {
            case '(':
                blankNode.getLexicalUnits().add(
                        new ListToken(this, getContext()));
                break;
            case '<':
                if (step() == '=') {
                    blankNode.getLexicalUnits().add(new Token("<="));
                    step();
                    discard();
                } else {
                    stepBack();
                    blankNode.getLexicalUnits().add(
                            new UriToken(this, getContext()));
                }
                break;
            case '_':
                blankNode.getLexicalUnits().add(
                        new BlankNodeToken(parseToken()));
                break;
            case '"':
                blankNode.getLexicalUnits().add(
                        new StringToken(this, getContext()));
                break;
            case '[':
                blankNode.getLexicalUnits().add(
                        new BlankNodeToken(this, getContext()));
                break;
            case '{':
                blankNode.getLexicalUnits().add(
                        new FormulaToken(this, getContext()));
                break;
            case ']':
                break;
            default:
                if (!isEndOfFile(getChar())) {
                    blankNode.getLexicalUnits().add(
                            new Token(this, getContext()));
                }
                break;
            }
        } while (!isEndOfFile(getChar()) && getChar() != ']');
        if (getChar() == ']') {
            // Set the cursor at the right of the list token.
            step();
        }

    }

    /**
     * Parses the given formula token.
     * 
     * @param formulaToken
     *            The formula token to parse.
     * @throws IOException
     */
    protected void parseFormula(FormulaToken formulaToken) throws IOException {
        step();
        do {
            parseStatement(new Context());
        } while (!isEndOfFile(getChar()) && getChar() != '}');
        if (getChar() == '}') {
            // Set the cursor at the right of the formula token.
            step();
        }
    }

    @Override
    protected void parseList(ListToken listToken) throws IOException {
        step();
        do {
            consumeWhiteSpaces();
            switch (getChar()) {
            case '(':
                listToken.getLexicalUnits().add(
                        new ListToken(this, getContext()));
                break;
            case '<':
                if (step() == '=') {
                    listToken.getLexicalUnits().add(new Token("<="));
                    step();
                    discard();
                } else {
                    stepBack();
                    listToken.getLexicalUnits().add(
                            new UriToken(this, getContext()));
                }
                break;
            case '_':
                listToken.getLexicalUnits().add(
                        new BlankNodeToken(this.parseToken()));
                break;
            case '"':
                listToken.getLexicalUnits().add(
                        new StringToken(this, getContext()));
                break;
            case '[':
                listToken.getLexicalUnits().add(
                        new BlankNodeToken(this, getContext()));
                break;
            case '{':
                listToken.getLexicalUnits().add(
                        new FormulaToken(this, getContext()));
                break;
            case ')':
                break;
            default:
                if (!isEndOfFile(getChar())) {
                    listToken.getLexicalUnits().add(
                            new Token(this, getContext()));
                }
                break;
            }
        } while (!isEndOfFile(getChar()) && getChar() != ')');
        if (getChar() == ')') {
            // Set the cursor at the right of the list token.
            step();
        }
    }

    @Override
    protected void parseStatement(Context context) throws IOException {
        List<LexicalUnit> lexicalUnits = new ArrayList<LexicalUnit>();
        do {
            consumeWhiteSpaces();
            switch (getChar()) {
            case '(':
                lexicalUnits.add(new ListToken(this, context));
                break;
            case '<':
                if (step() == '=') {
                    lexicalUnits.add(new Token("<="));
                    step();
                    discard();
                } else {
                    stepBack();
                    lexicalUnits.add(new UriToken(this, context));
                }
                break;
            case '_':
                lexicalUnits.add(new BlankNodeToken(parseToken()));
                break;
            case '"':
                lexicalUnits.add(new StringToken(this, context));
                break;
            case '[':
                lexicalUnits.add(new BlankNodeToken(this, context));
                break;
            case '!':
                lexicalUnits.add(new Token("!"));
                step();
                discard();
                break;
            case '^':
                lexicalUnits.add(new Token("^"));
                step();
                discard();
                break;
            case '=':
                if (step() == '>') {
                    lexicalUnits.add(new Token("=>"));
                    step();
                    discard();
                } else {
                    lexicalUnits.add(new Token("="));
                    discard();
                }
                break;
            case '@':
                // Remove the leading '@' character.
                step();
                discard();
                lexicalUnits.add(new Token(this, context));
                discard();
                break;
            case ';':
                step();
                discard();
                lexicalUnits.add(new Token(";"));
                break;
            case ',':
                step();
                discard();
                lexicalUnits.add(new Token(","));
                break;
            case '{':
                lexicalUnits.add(new FormulaToken(this, context));
                break;
            case '.':
                break;
            default:
                if (!isEndOfFile(getChar())) {
                    lexicalUnits.add(new Token(this, context));
                }
                break;
            }
        } while (!isEndOfFile(getChar()) && getChar() != '.'
                && getChar() != '}');

        // Generate the links
        generateLinks(lexicalUnits);
    }

}