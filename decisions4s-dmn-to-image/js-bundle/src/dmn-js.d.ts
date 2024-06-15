declare module 'dmn-js' {
    import { EventEmitter } from 'events';

    interface ViewerOptions {
        container: string | HTMLElement;
    }

    interface Viewbox {
        inner: { width: number; height: number };
    }

    interface Viewer extends EventEmitter {
        importXML: (xml: string) => void;
        getActiveViewer: () => Viewer;
        saveSVG: (callback: (err: Error | null, svg: string) => void) => void;
    }

    export default class DmnJS extends EventEmitter {
        constructor(options: ViewerOptions);
        importXML: (xml: string) => void;
        getActiveViewer: () => Viewer;
        saveSVG: (callback: (err: Error | null, svg: string) => void) => void;
    }
}