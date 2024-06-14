import DmnJS from 'dmn-js';
import {DmnViewerInterface, OpenOptions, Bounds, ImportDoneEvent} from './interface';

class DmnViewerImpl implements DmnViewerInterface {
    private dmnViewer: DmnJS | null = null;

    getViewer(): DmnJS {
        if (this.dmnViewer) {
            return this.dmnViewer;
        }

        this.dmnViewer = new DmnJS({
            container: '#canvas'
        });

        const x = this.dmnViewer
        this.dmnViewer.on('import.done', function (event: ImportDoneEvent) {
            const error = event.error;

            if (error) {
                console.error('could not import DMN diagram', error);
                return;
            }

            // zoom to fit full viewport
            x!.getActiveViewer().get('canvas').zoom('fit-viewport');
        });

        return this.dmnViewer;
    }

    openDiagram(dmnXML: string, options: OpenOptions = {}): Promise<Bounds> {
        const dmnViewer = this.getViewer();

        const minDimensions = options.minDimensions || {width: 0, height: 0};
        const title = options.title;
        const footer = options.footer;

        return new Promise<Bounds>((resolve, reject) => {
            dmnViewer.importXML(dmnXML, (err: Error) => {
                if (err) {
                    return reject(err);
                }

                const viewbox = dmnViewer.getActiveViewer().get('canvas').viewbox();
                const titleNode = document.querySelector('#title') as HTMLElement;

                if (title) {
                    titleNode.textContent = title;
                }

                titleNode.style.display = title ? 'block' : 'none';

                const width = Math.max(viewbox.inner.width, minDimensions.width);
                const diagramHeight = Math.max(viewbox.inner.height + (footer ? 90 : 0), minDimensions.height);

                const desiredViewport: Bounds = {
                    width,
                    height: diagramHeight + (footer ? 0 : 90),
                    diagramHeight
                };

                resolve(desiredViewport);
            });
        });
    }

    resize(): Promise<void> {
        const dmnViewer = this.getViewer();
        const canvas = dmnViewer.getActiveViewer().get('canvas');

        return new Promise<void>((resolve) => {
            canvas.resized();
            canvas.zoom('fit-viewport');
            resolve();
        });
    }

    toSVG(): Promise<string> {
        const dmnViewer = this.getViewer();

        return new Promise<string>((resolve, reject) => {
            dmnViewer.saveSVG((err: Error | null, svg: string) => {
                if (err) {
                    reject(err);
                } else {
                    resolve(svg);
                }
            });
        });
    }
}

// Make the interface available as a global variable
declare global {
    interface Window {
        dmnViewerInterface: DmnViewerInterface;
    }
}

window.dmnViewerInterface = new DmnViewerImpl();
