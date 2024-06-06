import useDocusaurusContext from "@docusaurus/useDocusaurusContext";
import React from 'react';
import CodeBlock from '@theme/CodeBlock';

interface SbtDependencyProps {
    moduleName: "decisions4s-core" | "decisions4s-dmn";
}

const SbtDependency: React.FC<SbtDependencyProps> = ({moduleName}) => {
    const {siteConfig} = useDocusaurusContext();
    const decisions4sVersion = siteConfig.customFields?.decisions4sVersion;
    return (
        <CodeBlock className="language-scala">
            {`"org.business4s" %% "${moduleName}" % "${decisions4sVersion}"`}
        </CodeBlock>
    );
}

export default SbtDependency;