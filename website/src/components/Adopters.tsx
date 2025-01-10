import React from 'react';

interface Adopter {
    name: string;
    link: string;
}

const Adopters: React.FC<{ name: string }> = ({ name }) => {
    const readmeSource = require(`!!raw-loader!../../../README.md`).default;

    // Extract adopters from the readme content
    const extractAdopters = (source: string): Adopter[] => {
        const match = source.match(/<!--- adopters_start -->([\s\S]*?)<!--- adopters_end -->/);
        if (!match || !match[1]) return [];

        // Extract individual adopters
        const adopterLines = match[1].trim().split('\n').filter(Boolean);
        return adopterLines.map(line => {
            const linkMatch = line.match(/\[(.+?)\]\((.+?)\)/);
            if (linkMatch) {
                return { name: linkMatch[1], link: linkMatch[2] };
            }
            return null;
        }).filter(Boolean) as Adopter[];
    };

    const adopters = extractAdopters(readmeSource);

    return (
        <div>
            <ul>
                {adopters.map((adopter, index) => (
                    <li key={index}>
                        <a href={adopter.link} target="_blank" rel="noopener noreferrer">
                            {adopter.name}
                        </a>
                    </li>
                ))}
            </ul>
        </div>
    );
};

export default Adopters;
