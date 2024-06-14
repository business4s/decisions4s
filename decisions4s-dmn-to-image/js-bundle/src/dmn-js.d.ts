declare module 'dmn-js' {
    import { EventEmitter } from 'events';

    interface ViewerOptions {
        container: string | HTMLElement;
    }

    interface ImportXMLCallback {
        (err: Error, warnings?: any[]): void;
    }

    interface Canvas {
        zoom: (type: string) => void;
        viewbox: () => Viewbox;
        resized: () => void;
    }

    interface Viewbox {
        inner: { width: number; height: number };
    }

    interface Viewer extends EventEmitter {
        importXML: (xml: string, callback: ImportXMLCallback) => void;
        get: (service: string) => Canvas;
        getActiveViewer: () => Viewer;
        saveSVG: (callback: (err: Error | null, svg: string) => void) => void;
    }

    export default class DmnJS extends EventEmitter {
        constructor(options: ViewerOptions);
        importXML: (xml: string, callback: ImportXMLCallback) => void;
        get: (service: string) => Canvas;
        getActiveViewer: () => Viewer;
        saveSVG: (callback: (err: Error | null, svg: string) => void) => void;
    }
}