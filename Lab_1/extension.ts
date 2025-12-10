import * as vscode from 'vscode';
import { CharStream, CommonTokenStream } from 'antlr4ng';
import { TorpedodepotEmailLexer } from './parser/grammar/TorpedodepotEmailLexer.js';
import { TorpedodepotEmailParser } from './parser/grammar/TorpedodepotEmailParser.js';

let decorationType: vscode.TextEditorDecorationType;

export function activate(context: vscode.ExtensionContext) {
    console.log('Torpedodepot Email Highlighter is now active!');

    // Create decoration type for pink highlighting
    decorationType = vscode.window.createTextEditorDecorationType({
        backgroundColor: 'rgba(255, 192, 203, 0.3)', // Pink with transparency
        border: '1px solid pink'
    });

    // Initial highlight
    if (vscode.window.activeTextEditor) {
        highlightEmails(vscode.window.activeTextEditor);
    }

    // Highlight when switching editors
    context.subscriptions.push(
        vscode.window.onDidChangeActiveTextEditor(editor => {
            if (editor) {
                highlightEmails(editor);
            }
        })
    );

    // Highlight when document changes
    context.subscriptions.push(
        vscode.workspace.onDidChangeTextDocument(event => {
            const editor = vscode.window.activeTextEditor;
            if (editor && event.document === editor.document) {
                highlightEmails(editor);
            }
        })
    );
}

function highlightEmails(editor: vscode.TextEditor) {
    const text = editor.document.getText();
    const decorations: vscode.DecorationOptions[] = [];

    // Find all potential email addresses using regex first for performance
    const emailPattern = /(?<![a-zA-Z0-9.])([a-zA-Z0-9]+)@torpedodepot\.(com|net|wannabemil)\b/g;
    let match;

    while ((match = emailPattern.exec(text)) !== null) {
        const potentialEmail = match[0];
        const startPos = match.index;

        // Validate with ANTLR parser
        if (validateEmail(potentialEmail)) {
            const startPosition = editor.document.positionAt(startPos);
            const endPosition = editor.document.positionAt(startPos + potentialEmail.length);
            
            const decoration: vscode.DecorationOptions = {
                range: new vscode.Range(startPosition, endPosition),
                hoverMessage: `Valid Torpedodepot Email: ${potentialEmail}`
            };
            
            decorations.push(decoration);
        }
    }

    editor.setDecorations(decorationType, decorations);
}

function validateEmail(email: string): boolean {
    try {
        const inputStream = CharStream.fromString(email);
        const lexer = new TorpedodepotEmailLexer(inputStream);
        const tokenStream = new CommonTokenStream(lexer);
        const parser = new TorpedodepotEmailParser(tokenStream);
        
        // Remove default error listeners to suppress console output
        parser.removeErrorListeners();
        
        // Try to parse as an email address
        const tree = parser.emailAddress();
        
        // Check if we consumed all tokens (reached EOF)
        return tokenStream.LT(1)?.type === TorpedodepotEmailParser.EOF;
    } catch (e) {
        return false;
    }
}

export function deactivate() {
    if (decorationType) {
        decorationType.dispose();
    }
}