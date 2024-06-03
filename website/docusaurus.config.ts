import {themes as prismThemes} from 'prism-react-renderer';
import type {Config} from '@docusaurus/types';
import type * as Preset from '@docusaurus/preset-classic';


const config: Config = {
    title: 'Decisions4s',
    tagline: 'Complicated conditionals made manageable',
    favicon: 'img/favicon.ico',

    // GitHub pages deployment config.
    url: 'https://business4s.github.io/',
    baseUrl: '/decisions4s/',
    organizationName: 'business4s',
    projectName: 'decisions4s',
    trailingSlash: true,

    onBrokenLinks: 'throw',
    onBrokenMarkdownLinks: 'warn',

    // Even if you don't use internationalization, you can use this field to set
    // useful metadata like html lang. For example, if your site is Chinese, you
    // may want to replace "en" with "zh-Hans".
    i18n: {
        defaultLocale: 'en',
        locales: ['en'],
    },

    presets: [
        [
            'classic',
            {
                docs: {
                    sidebarPath: './sidebars.ts',
                    editUrl: 'https://github.com/business4s/decisions4s/website',
                    remarkPlugins: [
                        [
                            require('remark-code-snippets'),
                            {baseDir: "../decisions-example/src/"}
                        ]
                    ],
                },
                theme: {
                    customCss: './src/css/custom.css',
                },
            } satisfies Preset.Options,
        ],
    ],

    themeConfig: {
        // Replace with your project's social card
        // image: 'img/docusaurus-social-card.jpg',
        navbar: {
            title: 'Decisions4s',
            logo: {
                alt: 'Decisions4s Logo',
                src: 'img/decisions4s-logo.drawio.svg',
            },
            items: [
                {
                    type: 'docSidebar',
                    sidebarId: 'tutorialSidebar',
                    position: 'left',
                    label: 'Docs',
                },
                {
                    href: 'https://github.com/Krever/workflows4s',
                    label: 'GitHub',
                    position: 'right',
                },
                {
                    href: 'https://bit.ly/business4s-discord',
                    label: 'Discord',
                    position: 'right',
                },
            ],
        },
        footer: {
            style: 'dark',
            links: [
                // {
                //   title: 'Docs',
                //   items: [
                //     {
                //       label: 'Docs',
                //       to: '/docs',
                //     },
                //   ],
                // },
                {
                  title: 'Community',
                  items: [
                    // {
                    //   label: 'Stack Overflow',
                    //   href: 'https://stackoverflow.com/questions/tagged/docusaurus',
                    // },
                    {
                      label: 'Discord',
                      href: 'https://bit.ly/business4s-discord',
                    },
                    {
                      label: 'Twitter',
                      href: 'https://twitter.com/business4scala',
                    },
                  ],
                },
                // {
                //   title: 'More',
                //   items: [
                //     {
                //       label: 'Blog',
                //       to: '/blog',
                //     },
                //     {
                //       label: 'GitHub',
                //       href: 'https://github.com/facebook/docusaurus',
                //     },
                //   ],
                // },
            ],
            // copyright: `Copyright © ${new Date().getFullYear()} My Project, Inc. Built with Docusaurus.`,
        },
        prism: {
            theme: prismThemes.github,
            darkTheme: prismThemes.dracula,
            additionalLanguages: ['java', 'scala', "json"]
        },
    } satisfies Preset.ThemeConfig,
    customFields: {
        decisions4sVersion: process.env.DECISIONS4s_VERSION,
    },
};

export default config;
