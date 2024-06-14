

export interface DmnViewerInterface {
    getViewer(): any;
    openDiagram(dmnXML: string, options?: OpenOptions): Promise<Bounds>;
    resize(): Promise<void>;
    toSVG(): Promise<string>;
}

export interface OpenOptions {
    minDimensions?: { width: number; height: number };
    title?: string;
    footer?: string;
}

export interface Dimensions {
    width: number;
    height: number;
}

export interface Bounds {
    width: number;
    height: number;
    diagramHeight: number;
}

export interface ImportDoneEvent {
    error: Error | null;
    warnings: Array<any>; // Adjust based on the actual warning type
}


export interface SkeletonGlobal {
    dmnViewerInterface: DmnViewerInterface;
}