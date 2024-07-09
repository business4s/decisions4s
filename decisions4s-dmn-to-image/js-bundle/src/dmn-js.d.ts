declare module 'dmn-js' {
    import { EventEmitter } from 'events';

    interface ViewerOptions {
        container: string | HTMLElement;
    }

    export default class DmnJS extends EventEmitter {
        constructor(options: ViewerOptions);
        importXML: (xml: string) => void;
    }
}