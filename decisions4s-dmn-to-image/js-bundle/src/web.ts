import DmnJS from 'dmn-js';
import {DmnViewerInterface, OpenOptions, Bounds, ImportDoneEvent} from './interface';

class DmnViewerImpl implements DmnViewerInterface {
    private dmnViewer: DmnJS | null = null;

    private getViewer(): DmnJS {
        if (this.dmnViewer) {
            return this.dmnViewer;
        }

        this.dmnViewer = new DmnJS({
            container: '#canvas',
        });

        this.dmnViewer.on('import.done', function (event: ImportDoneEvent) {
            const error = event.error;

            if (error) {
                console.error('could not import DMN diagram', error);
                return;
            }
        });

        return this.dmnViewer;
    }

    async openDiagram(dmnXML: string, options: OpenOptions = {}): Promise<Bounds> {
        const dmnViewer = this.getViewer();

        const minDimensions = options.minDimensions || {width: 0, height: 0};
        const title = options.title;
        const footer = options.footer;

        await dmnViewer.importXML(dmnXML);
        const titleNode = document.querySelector('#title') as HTMLElement;

        if (title) {
            titleNode.textContent = title;
        }

        titleNode.style.display = title ? 'block' : 'none';

        const width = minDimensions.width;
        const diagramHeight = minDimensions.height;

        const desiredViewport: Bounds = {
            width,
            height: diagramHeight + (footer ? 0 : 90),
            diagramHeight
        };
        return desiredViewport;
    }
}

// Make the interface available as a global variable
declare global {
    interface Window {
        dmnViewerInterface: DmnViewerInterface;
    }
}

window.dmnViewerInterface = new DmnViewerImpl();
