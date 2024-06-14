import puppeteer, {Page} from 'puppeteer';

import {DmnViewerInterface, SkeletonGlobal} from './interface';


async function printDiagram(page: Page, diagramXML: string): Promise<string> {
    const dir = __dirname;
    await page.goto(`file://${dir}/skeleton.html`, {
        waitUntil: 'load'
    });

    const loadXML = async function (diagramXML: string) {

        const viewer: DmnViewerInterface = (window as any as SkeletonGlobal).dmnViewerInterface;
        // Open the diagram
        await viewer.openDiagram(diagramXML);

        const sleep = ms => new Promise(res => setTimeout(res, ms));
        await sleep(100000)

        const {width, height} = {width: 800, height: 600};

        return {
            width,
            height,
            diagramHeight: height
        };
    }

    const desiredViewport = await page.evaluate(loadXML, diagramXML);

    await page.setViewport({
        width: Math.round(desiredViewport.width),
        height: Math.round(desiredViewport.height),
    });

    // Resize the page
    await page.evaluate(() => {
        window.dispatchEvent(new Event('resize'));
    });

    // Capture the screenshot as base64
    return await page.screenshot({encoding: 'base64'});
}

async function withPage(fn: (page: Page) => Promise<string>): Promise<string> {
    const browser = await puppeteer.launch({
        headless: false,
        args: [
            '--no-sandbox',
            '--disable-setuid-sandbox',
            '--disable-web-security',
            '--disable-features=IsolateOrigins',
            '--disable-site-isolation-trials',
        ]
    });

    try {
        const page = await browser.newPage();
        return await fn(page);
    } finally {
        await browser.close();
    }
}

export async function convert(diagramXML: string): Promise<string> {
    return await withPage(async (page) => {
        return await printDiagram(page, diagramXML);
    });
}
