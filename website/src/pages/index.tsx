import clsx from 'clsx';
import Link from '@docusaurus/Link';
import useDocusaurusContext from '@docusaurus/useDocusaurusContext';
import Layout from '@theme/Layout';
import HomepageFeatures from '@site/src/components/HomepageFeatures';
import Heading from '@theme/Heading';
import CodeBlock from '@theme/CodeBlock';

import styles from './index.module.css';


function HomepageHeader() {
    const {siteConfig} = useDocusaurusContext();
    const decisions4sVersion = siteConfig.customFields?.decisions4sVersion;
    return (
        <header className={clsx('hero hero--primary', styles.heroBanner)}>
            <div className="container">
                <div className="text--center">
                    <img className={styles.roundedImage} src="img/decisions4s-logo.drawio.svg"/>
                </div>
                <Heading as="h1" className="hero__title">
                    {siteConfig.title}
                </Heading>
                <p className="hero__subtitle">{siteConfig.tagline}</p>
                <div className={styles.dependency}>
                    <CodeBlock className="language-scala">
                        {`"org.business4s" %% "decisions4s-core" % "${decisions4sVersion}"`}
                    </CodeBlock>
                </div>
                <div className={styles.buttons}>
                    <Link
                        className={`button button--secondary button--lg ${styles.myButton}`}
                        to="">
                        Try in Scastie
                    </Link>
                    <Link
                        className={`button button--secondary button--lg ${styles.myButton}`}
                        to="/docs">
                        Read the Docs
                    </Link>
                </div>
            </div>
        </header>
    );
}

export default function Home(): JSX.Element {
    const {siteConfig} = useDocusaurusContext();
    return (
        <Layout
            title={siteConfig.title}
            description={siteConfig.tagline}>
            <HomepageHeader/>
            <main>
                <HomepageFeatures/>
            </main>
        </Layout>
    );
}
