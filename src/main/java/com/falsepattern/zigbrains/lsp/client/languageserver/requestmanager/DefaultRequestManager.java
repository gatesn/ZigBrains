/*
 * Copyright 2023 FalsePattern
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.falsepattern.zigbrains.lsp.client.languageserver.requestmanager;

import com.intellij.openapi.diagnostic.Logger;
import org.eclipse.lsp4j.ApplyWorkspaceEditParams;
import org.eclipse.lsp4j.ApplyWorkspaceEditResponse;
import org.eclipse.lsp4j.CodeAction;
import org.eclipse.lsp4j.CodeActionOptions;
import org.eclipse.lsp4j.CodeActionParams;
import org.eclipse.lsp4j.CodeLens;
import org.eclipse.lsp4j.CodeLensParams;
import org.eclipse.lsp4j.ColorInformation;
import org.eclipse.lsp4j.ColorPresentation;
import org.eclipse.lsp4j.ColorPresentationParams;
import org.eclipse.lsp4j.Command;
import org.eclipse.lsp4j.CompletionItem;
import org.eclipse.lsp4j.CompletionList;
import org.eclipse.lsp4j.CompletionOptions;
import org.eclipse.lsp4j.CompletionParams;
import org.eclipse.lsp4j.DefinitionParams;
import org.eclipse.lsp4j.DidChangeConfigurationParams;
import org.eclipse.lsp4j.DidChangeTextDocumentParams;
import org.eclipse.lsp4j.DidChangeWatchedFilesParams;
import org.eclipse.lsp4j.DidCloseTextDocumentParams;
import org.eclipse.lsp4j.DidOpenTextDocumentParams;
import org.eclipse.lsp4j.DidSaveTextDocumentParams;
import org.eclipse.lsp4j.DocumentColorParams;
import org.eclipse.lsp4j.DocumentFormattingParams;
import org.eclipse.lsp4j.DocumentHighlight;
import org.eclipse.lsp4j.DocumentHighlightParams;
import org.eclipse.lsp4j.DocumentLink;
import org.eclipse.lsp4j.DocumentLinkParams;
import org.eclipse.lsp4j.DocumentOnTypeFormattingParams;
import org.eclipse.lsp4j.DocumentRangeFormattingParams;
import org.eclipse.lsp4j.DocumentSymbol;
import org.eclipse.lsp4j.DocumentSymbolParams;
import org.eclipse.lsp4j.ExecuteCommandParams;
import org.eclipse.lsp4j.FoldingRange;
import org.eclipse.lsp4j.FoldingRangeRequestParams;
import org.eclipse.lsp4j.Hover;
import org.eclipse.lsp4j.HoverParams;
import org.eclipse.lsp4j.ImplementationParams;
import org.eclipse.lsp4j.InitializeParams;
import org.eclipse.lsp4j.InitializeResult;
import org.eclipse.lsp4j.InitializedParams;
import org.eclipse.lsp4j.Location;
import org.eclipse.lsp4j.LocationLink;
import org.eclipse.lsp4j.MessageActionItem;
import org.eclipse.lsp4j.MessageParams;
import org.eclipse.lsp4j.PublishDiagnosticsParams;
import org.eclipse.lsp4j.ReferenceParams;
import org.eclipse.lsp4j.RegistrationParams;
import org.eclipse.lsp4j.RenameParams;
import org.eclipse.lsp4j.ServerCapabilities;
import org.eclipse.lsp4j.ShowMessageRequestParams;
import org.eclipse.lsp4j.SignatureHelp;
import org.eclipse.lsp4j.SignatureHelpParams;
import org.eclipse.lsp4j.SymbolInformation;
import org.eclipse.lsp4j.TextDocumentPositionParams;
import org.eclipse.lsp4j.TextDocumentSyncOptions;
import org.eclipse.lsp4j.TextEdit;
import org.eclipse.lsp4j.TypeDefinitionParams;
import org.eclipse.lsp4j.UnregistrationParams;
import org.eclipse.lsp4j.WillSaveTextDocumentParams;
import org.eclipse.lsp4j.WorkspaceEdit;
import org.eclipse.lsp4j.WorkspaceSymbol;
import org.eclipse.lsp4j.WorkspaceSymbolParams;
import org.eclipse.lsp4j.jsonrpc.messages.Either;
import org.eclipse.lsp4j.services.LanguageClient;
import org.eclipse.lsp4j.services.LanguageServer;
import org.eclipse.lsp4j.services.TextDocumentService;
import org.eclipse.lsp4j.services.WorkspaceService;
import com.falsepattern.zigbrains.lsp.client.languageserver.ServerStatus;
import com.falsepattern.zigbrains.lsp.client.languageserver.wrapper.LanguageServerWrapper;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * Default implementation for LSP requests/notifications handling.
 */
public class DefaultRequestManager implements RequestManager {

    private final Logger LOG = Logger.getInstance(DefaultRequestManager.class);

    private final LanguageServerWrapper wrapper;
    private final LanguageServer server;
    private final LanguageClient client;
    private final ServerCapabilities serverCapabilities;
    private final TextDocumentSyncOptions textDocumentOptions;
    private final WorkspaceService workspaceService;
    private final TextDocumentService textDocumentService;

    public DefaultRequestManager(LanguageServerWrapper wrapper, LanguageServer server, LanguageClient client,
                                 ServerCapabilities serverCapabilities) {

        this.wrapper = wrapper;
        this.server = server;
        this.client = client;
        this.serverCapabilities = serverCapabilities;

        textDocumentOptions = serverCapabilities.getTextDocumentSync().isRight() ? serverCapabilities.getTextDocumentSync().getRight() : null;
        workspaceService = server.getWorkspaceService();
        textDocumentService = server.getTextDocumentService();
    }

    public LanguageServerWrapper getWrapper() {
        return wrapper;
    }

    public LanguageClient getClient() {
        return client;
    }

    public LanguageServer getServer() {
        return server;
    }

    public ServerCapabilities getServerCapabilities() {
        return serverCapabilities;
    }

    // Client
    @Override
    public void showMessage(MessageParams messageParams) {
        client.showMessage(messageParams);
    }

    @Override
    public CompletableFuture<MessageActionItem> showMessageRequest(ShowMessageRequestParams showMessageRequestParams) {
        return client.showMessageRequest(showMessageRequestParams);
    }

    @Override
    public void logMessage(MessageParams messageParams) {
        client.logMessage(messageParams);
    }

    @Override
    public void telemetryEvent(Object o) {
        client.telemetryEvent(o);
    }

    @Override
    public CompletableFuture<Void> registerCapability(RegistrationParams params) {
        return client.registerCapability(params);
    }

    @Override
    public CompletableFuture<Void> unregisterCapability(UnregistrationParams params) {
        return client.unregisterCapability(params);
    }

    @Override
    public CompletableFuture<ApplyWorkspaceEditResponse> applyEdit(ApplyWorkspaceEditParams params) {
        return client.applyEdit(params);
    }

    @Override
    public void publishDiagnostics(PublishDiagnosticsParams publishDiagnosticsParams) {
        client.publishDiagnostics(publishDiagnosticsParams);
    }

    @Override
    public CompletableFuture<Void> refreshSemanticTokens() {
        return client.refreshSemanticTokens();
    }

    // Server

    // General
    @Override
    public CompletableFuture<InitializeResult> initialize(InitializeParams params) {
        if (checkStatus()) {
            try {
                return server.initialize(params);
            } catch (Exception e) {
                crashed(e);
                return null;
            }
        } else {
            return null;
        }
    }

    @Override
    public void initialized(InitializedParams params) {
        if (wrapper.getStatus() == ServerStatus.STARTED) {
            try {
                server.initialized(params);
            } catch (Exception e) {
                crashed(e);
            }
        }
    }

    @Override
    public CompletableFuture<Object> shutdown() {
        if (checkStatus()) {
            try {
                return server.shutdown();
            } catch (Exception e) {
                crashed(e);
                return null;
            }
        }
        return null;
    }

    @Override
    public void exit() {
        if (checkStatus()) {
            try {
                server.exit();
            } catch (Exception e) {
                crashed(e);
            }
        }
    }

    @Override
    public TextDocumentService getTextDocumentService() {
        if (checkStatus()) {
            try {
                return textDocumentService;
            } catch (Exception e) {
                crashed(e);
                return null;
            }
        }
        return null;
    }

    @Override
    public WorkspaceService getWorkspaceService() {
        if (checkStatus()) {
            try {
                return workspaceService;
            } catch (Exception e) {
                crashed(e);
                return null;
            }
        } else {
            return null;
        }
    }

    // Workspace service
    @Override
    public void didChangeConfiguration(DidChangeConfigurationParams params) {
        if (checkStatus()) {
            try {
                workspaceService.didChangeConfiguration(params);
            } catch (Exception e) {
                crashed(e);
            }
        }
    }

    @Override
    public void didChangeWatchedFiles(DidChangeWatchedFilesParams params) {
        if (checkStatus()) {
            try {
                workspaceService.didChangeWatchedFiles(params);
            } catch (Exception e) {
                crashed(e);
            }
        }
    }

    public CompletableFuture<Either<List<? extends SymbolInformation>, List<? extends WorkspaceSymbol>>> symbol(WorkspaceSymbolParams params) {
        if (checkStatus()) {
            try {
                return Optional.ofNullable(serverCapabilities.getWorkspaceSymbolProvider())
                        .map(e -> e.getLeft() || e.getRight() != null).orElse(false) ?
                        workspaceService.symbol(params) : null;
            } catch (Exception e) {
                crashed(e);
                return null;
            }
        } else
            return null;
    }

    public CompletableFuture<Object> executeCommand(ExecuteCommandParams params) {
        if (checkStatus()) {
            try {
                return serverCapabilities.getExecuteCommandProvider() != null ? workspaceService.executeCommand(params) : null;
            } catch (Exception e) {
                crashed(e);
                return null;
            }
        }
        return null;
    }

    // Text document service
    @Override
    public void didOpen(DidOpenTextDocumentParams params) {
        if (checkStatus()) {
            try {
                if (Optional.ofNullable(textDocumentOptions).map(TextDocumentSyncOptions::getOpenClose).orElse(false)) {
                    textDocumentService.didOpen(params);
                }
            } catch (Exception e) {
                crashed(e);
            }
        }
    }

    @Override
    public void didChange(DidChangeTextDocumentParams params) {
        if (checkStatus()) {
            try {
                if (textDocumentOptions == null || textDocumentOptions.getChange() != null) {
                    textDocumentService.didChange(params);
                }
            } catch (Exception e) {
                crashed(e);
            }
        }
    }

    @Override
    public void willSave(WillSaveTextDocumentParams params) {
        if (checkStatus()) {
            try {
                if (Optional.ofNullable(textDocumentOptions).map(x -> x.getWillSave()).orElse(false)) {
                    textDocumentService.willSave(params);
                }
            } catch (Exception e) {
                crashed(e);
            }
        }
    }

    @Override
    public CompletableFuture<List<TextEdit>> willSaveWaitUntil(WillSaveTextDocumentParams params) {
        if (checkStatus()) {
            try {
                return Optional.ofNullable(textDocumentOptions).map(x -> x.getWillSaveWaitUntil()).orElse(false) ?
                        textDocumentService.willSaveWaitUntil(params) : null;
            } catch (Exception e) {
                crashed(e);
                return null;
            }
        }
        return null;
    }

    @Override
    public void didSave(DidSaveTextDocumentParams params) {
        if (checkStatus()) {
            try {
                if (Optional.ofNullable(textDocumentOptions).map(x -> x.getSave()).isPresent()) {
                    textDocumentService.didSave(params);
                }
            } catch (Exception e) {
                crashed(e);
            }
        }
    }

    @Override
    public void didClose(DidCloseTextDocumentParams params) {
        if (checkStatus()) {
            try {
                if (Optional.ofNullable(textDocumentOptions).map(TextDocumentSyncOptions::getOpenClose).orElse(false)) {
                    textDocumentService.didClose(params);
                }
            } catch (Exception e) {
                crashed(e);
            }
        }
    }

    @Override
    public CompletableFuture<Either<List<CompletionItem>, CompletionList>> completion(CompletionParams params) {
        if (checkStatus()) {
            try {
                return (serverCapabilities.getCompletionProvider() != null) ? textDocumentService.completion(params) : null;
            } catch (Exception e) {
                crashed(e);
                return null;
            }
        }
        return null;
    }

    @Override
    public CompletableFuture<CompletionItem> resolveCompletionItem(CompletionItem unresolved) {
        if (checkStatus()) {
            try {
                return (Optional.ofNullable(serverCapabilities.getCompletionProvider()).map(CompletionOptions::getResolveProvider).orElse(false)) ?
                        textDocumentService.resolveCompletionItem(unresolved) : null;
            } catch (Exception e) {
                crashed(e);
                return null;
            }
        }
        return null;
    }

    @Override
    public CompletableFuture<Hover> hover(TextDocumentPositionParams params) {
        return hover(new HoverParams(params.getTextDocument(), params.getPosition()));
    }

    @Override
    public CompletableFuture<Hover> hover(HoverParams params) {
        if (checkStatus()) {
            try {
                return
                        Optional.ofNullable(serverCapabilities.getHoverProvider())
                                .map(e -> e.getRight() != null || (e.getLeft() != null && e.getLeft())).orElse(false) ?
                                textDocumentService.hover(params) : null;

            } catch (Exception e) {
                crashed(e);
                return null;
            }
        }
        return null;
    }

    @Override
    public CompletableFuture<SignatureHelp> signatureHelp(TextDocumentPositionParams params) {
        return signatureHelp(new SignatureHelpParams(params.getTextDocument(), params.getPosition()));
    }

    @Override
    public CompletableFuture<SignatureHelp> signatureHelp(SignatureHelpParams params) {
        if (checkStatus()) {
            try {
                return (serverCapabilities.getSignatureHelpProvider() != null) ? textDocumentService.signatureHelp(params) : null;
            } catch (Exception e) {
                crashed(e);
                return null;
            }
        }
        return null;
    }

    @Override
    public CompletableFuture<List<? extends Location>> references(ReferenceParams params) {
        if (checkStatus()) {
            try {
                return Optional.ofNullable(serverCapabilities.getReferencesProvider())
                        .map(e -> e.getLeft() || e.getRight() != null).orElse(false) ?
                        textDocumentService.references(params) : null;
            } catch (Exception e) {
                crashed(e);
                return null;
            }
        }
        return null;
    }

    @Override
    public CompletableFuture<List<? extends DocumentHighlight>> documentHighlight(TextDocumentPositionParams params) {
        return documentHighlight(new DocumentHighlightParams(params.getTextDocument(), params.getPosition()));
    }

    @Override
    public CompletableFuture<List<? extends DocumentHighlight>> documentHighlight(DocumentHighlightParams params) {
        if (checkStatus()) {
            try {
                return Optional.ofNullable(serverCapabilities.getDocumentHighlightProvider())
                        .map(e -> e.getLeft() || e.getRight() != null).orElse(false) ?
                        textDocumentService.documentHighlight(params) : null;
            } catch (Exception e) {
                crashed(e);
                return null;
            }
        }
        return null;
    }

    @Override
    public CompletableFuture<List<Either<SymbolInformation, DocumentSymbol>>> documentSymbol(DocumentSymbolParams params) {
        if (checkStatus()) {
            try {
                return Optional.ofNullable(serverCapabilities.getDocumentSymbolProvider())
                        .map(e -> e.getLeft() || e.getRight() != null).orElse(false) ?
                        textDocumentService.documentSymbol(params) : null;
            } catch (Exception e) {
                crashed(e);
                return null;
            }
        }
        return null;
    }

    @Override
    public CompletableFuture<List<? extends TextEdit>> formatting(DocumentFormattingParams params) {
        if (checkStatus()) {
            try {
                return Optional.ofNullable(serverCapabilities.getDocumentFormattingProvider())
                        .map(e -> e.getLeft() || e.getRight() != null).orElse(false) ?
                        textDocumentService.formatting(params) : null;
            } catch (Exception e) {
                crashed(e);
                return null;
            }
        } else {
            return null;
        }
    }

    @Override
    public CompletableFuture<List<? extends TextEdit>> rangeFormatting(DocumentRangeFormattingParams params) {
        if (checkStatus()) {
            try {
                return (serverCapabilities.getDocumentRangeFormattingProvider() != null) ? textDocumentService.rangeFormatting(params) : null;
            } catch (Exception e) {
                crashed(e);
                return null;
            }
        }
        return null;
    }

    @Override
    public CompletableFuture<List<? extends TextEdit>> onTypeFormatting(DocumentOnTypeFormattingParams params) {
        if (checkStatus()) {
            try {
                return (serverCapabilities.getDocumentOnTypeFormattingProvider() != null) ?
                        textDocumentService.onTypeFormatting(params) : null;
            } catch (Exception e) {
                crashed(e);
                return null;
            }
        }
        return null;
    }

    @Override
    public CompletableFuture<Either<List<? extends Location>, List<? extends LocationLink>>> definition(TextDocumentPositionParams params) {
        return definition(new DefinitionParams(params.getTextDocument(), params.getPosition()));
    }

    @Override
    public CompletableFuture<Either<List<? extends Location>, List<? extends LocationLink>>> definition(DefinitionParams params) {
        if (checkStatus()) {
            try {
                return Optional.ofNullable(serverCapabilities.getDefinitionProvider())
                        .map(e -> e.getLeft() || e.getRight() != null).orElse(false) ?
                        textDocumentService.definition(params) : null;
            } catch (Exception e) {
                crashed(e);
                return null;
            }
        }
        return null;
    }

    @Override
    public CompletableFuture<List<Either<Command, CodeAction>>> codeAction(CodeActionParams params) {
        if (checkStatus()) {
            try {
                return checkCodeActionProvider(serverCapabilities.getCodeActionProvider()) ? textDocumentService.codeAction(params) : null;
            } catch (Exception e) {
                crashed(e);
                return null;
            }
        }
        return null;
    }

    @Override
    public CompletableFuture<List<? extends CodeLens>> codeLens(CodeLensParams params) {
        if (checkStatus()) {
            try {
                return (serverCapabilities.getCodeLensProvider() != null) ? textDocumentService.codeLens(params) : null;
            } catch (Exception e) {
                crashed(e);
                return null;
            }
        }
        return null;
    }

    @Override
    public CompletableFuture<CodeLens> resolveCodeLens(CodeLens unresolved) {
        if (checkStatus()) {
            try {
                return (serverCapabilities.getCodeLensProvider() != null && serverCapabilities.getCodeLensProvider()
                        .getResolveProvider()) ? textDocumentService.resolveCodeLens(unresolved) : null;
            } catch (Exception e) {
                crashed(e);
                return null;
            }
        }
        return null;
    }

    @Override
    public CompletableFuture<List<DocumentLink>> documentLink(DocumentLinkParams params) {
        if (checkStatus()) {
            try {
                return (serverCapabilities.getDocumentLinkProvider() != null) ?
                        textDocumentService.documentLink(params) :
                        null;
            } catch (Exception e) {
                crashed(e);
                return null;
            }
        }
        return null;
    }

    @Override
    public CompletableFuture<DocumentLink> documentLinkResolve(DocumentLink unresolved) {
        if (checkStatus()) {
            try {
                return serverCapabilities.getDocumentLinkProvider() != null && Optional.ofNullable(serverCapabilities
                        .getDocumentLinkProvider().getResolveProvider()).orElse(false) ?
                        textDocumentService.documentLinkResolve(unresolved) :
                        null;
            } catch (Exception e) {
                crashed(e);
                return null;
            }
        }
        return null;
    }

    @Override
    public CompletableFuture<WorkspaceEdit> rename(RenameParams params) {
        //        if (checkStatus()) {
        //            try {
        //                return (checkProvider((Either<Boolean, StaticRegistrationOptions>)serverCapabilities.getRenameProvider())) ?
        //                        textDocumentService.rename(params) :
        //                        null;
        //            } catch (Exception e) {
        //                crashed(e);
        //                return null;
        //            }
        //        }
        return null;
    }

    @Override
    public CompletableFuture<Either<List<? extends Location>, List<? extends LocationLink>>> implementation(ImplementationParams params) {
        return null;
    }

    @Override
    public CompletableFuture<Either<List<? extends Location>, List<? extends LocationLink>>> typeDefinition(TypeDefinitionParams params) {
        return null;
    }

    @Override
    public CompletableFuture<List<ColorInformation>> documentColor(DocumentColorParams params) {
        return null;
    }

    @Override
    public CompletableFuture<List<ColorPresentation>> colorPresentation(ColorPresentationParams params) {
        return null;
    }

    @Override
    public CompletableFuture<List<FoldingRange>> foldingRange(FoldingRangeRequestParams params) {
        if (checkStatus()) {
            try {
                var future = serverCapabilities.getFoldingRangeProvider() != null ?
                        textDocumentService.foldingRange(params) :
                        null;
                return future == null ? null : future.thenApply((range) -> range == null ? Collections.emptyList() : range);
            } catch (Exception e) {
                crashed(e);
                return null;
            }
        }
        return null;
    }

    public boolean checkStatus() {
        return wrapper.getStatus() == ServerStatus.INITIALIZED;
    }

    private void crashed(Exception e) {
        LOG.warn(e);
        wrapper.crashed(e);
    }

    private boolean checkCodeActionProvider(Either<Boolean, CodeActionOptions> provider) {
        return provider != null && ((provider.isLeft() && provider.getLeft()) || (provider.isRight()
                && provider.getRight() != null));
    }
}
