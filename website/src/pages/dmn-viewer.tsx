import React, {useCallback, useEffect, useRef, useState} from 'react';
import BrowserOnly from '@docusaurus/BrowserOnly';
import Layout from '@theme/Layout';

import styles from './dmn-viewer.module.css';

import 'dmn-js/dist/assets/diagram-js.css';
import 'dmn-js/dist/assets/dmn-js-shared.css';
import 'dmn-js/dist/assets/dmn-js-drd.css';
import 'dmn-js/dist/assets/dmn-js-decision-table.css';
import 'dmn-js/dist/assets/dmn-js-decision-table-controls.css';
import 'dmn-js/dist/assets/dmn-js-literal-expression.css';
import 'dmn-js/dist/assets/dmn-font/css/dmn.css';

const HASH_PREFIX = '#dmn=';

function decodeHash(hash: string): Uint8Array | null {
    if (!hash.startsWith(HASH_PREFIX)) return null;
    try {
        const b64 = hash.slice(HASH_PREFIX.length).replace(/-/g, '+').replace(/_/g, '/');
        const padded = b64 + '='.repeat((4 - (b64.length % 4)) % 4);
        const binary = atob(padded);
        const bytes = new Uint8Array(binary.length);
        for (let i = 0; i < binary.length; i++) bytes[i] = binary.charCodeAt(i);
        return bytes;
    } catch {
        return null;
    }
}

function encodeXml(xml: string, pako: typeof import('pako')): string {
    const deflated = pako.deflate(new TextEncoder().encode(xml), {level: 9});
    let binary = '';
    for (let i = 0; i < deflated.length; i++) binary += String.fromCharCode(deflated[i]);
    return btoa(binary).replace(/\+/g, '-').replace(/\//g, '_').replace(/=+$/, '');
}

type State =
    | {tag: 'empty'}
    | {tag: 'ready'}
    | {tag: 'error'; message: string};

function Viewer(): JSX.Element {
    const containerRef = useRef<HTMLDivElement>(null);
    const viewerRef = useRef<any>(null);
    const [state, setState] = useState<State>({tag: 'empty'});
    const [copied, setCopied] = useState(false);

    const renderXml = useCallback(async (xml: string) => {
        try {
            const pako = await import('pako');
            const DmnJSModule: any = await import('dmn-js');
            if (!viewerRef.current) {
                viewerRef.current = new DmnJSModule.default({container: containerRef.current});
            }
            await viewerRef.current.importXML(xml);
            const encoded = encodeXml(xml, pako);
            window.history.replaceState(null, '', HASH_PREFIX + encoded);
            setState({tag: 'ready'});
        } catch (e) {
            setState({tag: 'error', message: `Failed to render DMN: ${(e as Error).message}`});
        }
    }, []);

    useEffect(() => () => {
        viewerRef.current?.destroy?.();
        viewerRef.current = null;
    }, []);

    useEffect(() => {
        const compressed = decodeHash(window.location.hash);
        if (!compressed) return;
        let cancelled = false;
        (async () => {
            try {
                const pako = await import('pako');
                const xml = pako.inflate(compressed, {to: 'string'});
                if (!cancelled) await renderXml(xml);
            } catch (e) {
                if (!cancelled) {
                    setState({tag: 'error', message: `Failed to decode share URL: ${(e as Error).message}`});
                }
            }
        })();
        return () => {
            cancelled = true;
        };
    }, [renderXml]);

    const onFile = useCallback((file: File) => {
        const reader = new FileReader();
        reader.onload = () => renderXml(String(reader.result));
        reader.onerror = () => {
            setState({tag: 'error', message: `Failed to read file: ${reader.error?.message ?? 'unknown'}`});
        };
        reader.readAsText(file);
    }, [renderXml]);

    const onPickFile = useCallback((e: React.ChangeEvent<HTMLInputElement>) => {
        const f = e.target.files?.[0];
        if (f) onFile(f);
    }, [onFile]);

    const onDrop = useCallback((e: React.DragEvent) => {
        e.preventDefault();
        const f = e.dataTransfer.files?.[0];
        if (f) onFile(f);
    }, [onFile]);

    const onCopy = useCallback(async () => {
        try {
            await navigator.clipboard.writeText(window.location.href);
            setCopied(true);
            setTimeout(() => setCopied(false), 1500);
        } catch (e) {
            console.error('Failed to copy share link:', e);
        }
    }, []);

    return (
        <div className={styles.root} onDragOver={e => e.preventDefault()} onDrop={onDrop}>
            <div className={styles.toolbar}>
                <label className={`button button--secondary button--sm ${styles.fileLabel}`}>
                    Open .dmn file
                    <input type="file" accept=".dmn,.xml,application/xml,text/xml"
                           onChange={onPickFile} className={styles.hiddenInput}/>
                </label>
                {state.tag === 'ready' && (
                    <button className="button button--primary button--sm" onClick={onCopy}>
                        {copied ? 'Copied!' : 'Copy share link'}
                    </button>
                )}
                <span className={styles.hint}>
                    Or drop a .dmn file anywhere on this page.
                </span>
            </div>

            <div className={styles.scroller}>
                <div ref={containerRef} className={styles.canvas}/>
                {state.tag !== 'ready' && (
                    <div className={`${styles.placeholder} ${state.tag === 'error' ? styles.placeholderError : ''}`}>
                        {state.tag === 'error'
                            ? state.message
                            : 'Pick a DMN file to render, or open a share link to view someone else\'s.'}
                    </div>
                )}
            </div>
        </div>
    );
}

export default function DmnViewerPage(): JSX.Element {
    return (
        <Layout title="DMN Viewer" description="Render and share DMN decision diagrams">
            <BrowserOnly fallback={<div className={styles.loadingFallback}>Loading viewer…</div>}>
                {() => <Viewer/>}
            </BrowserOnly>
        </Layout>
    );
}
