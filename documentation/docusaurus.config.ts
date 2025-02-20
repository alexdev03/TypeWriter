import { themes } from 'prism-react-renderer';
import { Config } from '@docusaurus/types';
import { Options } from '@docusaurus/plugin-content-docs';

const lightTheme = themes.vsLight;
const darkTheme = themes.vsDark;
// Initialize author data caching

const url = 'https://docs.typewritermc.com';

// Common configuration for docs
const docsCommon: Options = {
  sidebarPath: require.resolve('./sidebars.js'),
  editUrl: 'https://github.com/gabber235/TypeWriter/tree/develop/documentation/',
  showLastUpdateAuthor: true,
  showLastUpdateTime: true,
};

const config: Config = {
  title: 'Typewriter',
  tagline: 'The next generation of Player Interactions',
  favicon: 'img/favicon.ico',
  url: url,
  baseUrl: '/',
  trailingSlash: false,
  
  // Basic configuration
  organizationName: 'gabber235',
  projectName: 'Typewriter',
  onBrokenLinks: 'throw',
  onBrokenMarkdownLinks: 'warn',
  clientModules: [
    require.resolve("./src/css/custom.css") 
  ],
  i18n: {
    defaultLocale: 'en',
    locales: ['en'],
  },

  markdown: {
    mermaid: true,
  },

  themes: [
    '@docusaurus/theme-classic',
    '@docusaurus/theme-mermaid',
    '@docusaurus/theme-search-algolia',
  ],

  // Main documentation plugin
  plugins: [
    [
      '@docusaurus/plugin-content-docs',
      {
        ...docsCommon,
        routeBasePath: '/',
        lastVersion: '0.7.0',
        versions: {
          current: {
            label: 'Beta ⚠️',
            path: 'beta',
          },
        },
      },
    ],
    [
      '@docusaurus/plugin-content-blog',
      {
        id: 'blog',
        routeBasePath: 'devlog',
        path: './devlog',
        blogSidebarCount: 'ALL',
        blogSidebarTitle: 'All posts',
      },
    ],
    [
        '@docusaurus/plugin-content-pages',
        {
                path: 'src/pages',
                routeBasePath: '',
                include: ['**/*.{js,jsx,ts,tsx,md,mdx}'],
                exclude: [
                  '**/_*.{js,jsx,ts,tsx,md,mdx}',
                  '**/_*/**',
                  '**/*.test.{js,jsx,ts,tsx}',
                  '**/__tests__/**',
                ],
                mdxPageComponent: '@theme/MDXPage',
        },
    ],
    "rive-loader",
    require.resolve("./plugins/tailwind/index.js"),
    require.resolve("./plugins/compression/index.js"),
    require.resolve("./plugins/code-snippets/index.js"),
    [
      "posthog-docusaurus",
      {
        apiKey: process.env.POSTHOG_API_KEY ?? true,
        appUrl: "https://research.typewritermc.com",
        enable_heatmaps: true,
        enableInDevelopment: false,
      },
    ],
  ],

  // Theme configuration  
  themeConfig:
  /** @type {import('@docusaurus/preset-classic').ThemeConfig} */
  ({
      theme: {
          customCss: require.resolve('./src/css/custom.css'),
      },
      announcementBar: {
          id: 'support_us',
          content:
              'TypeWriter 0.7.0 is out!',
          isCloseable: true,
      },
      mermaid: {
          theme: { light: 'base', dark: 'base' },
          options: {
              width: 150,
          },
      },
      metadata: [
          { name: 'keywords', content: 'Minecraft, Questing, NPC\'s, Manifests, Cinematics, sequences, Road-network, Interactions, player interactions, plugin, papermc' },
          { name: 'robots', content: 'index, follow' },
          { name: 'description', content: 'TypeWriter is a plugin for Minecraft servers that allows you to create complex interactions between players and the server. It allows you to create sequences, cinematics, quests, NPCs, manifests, road-networks, and more!' },
          { name: 'Content-Type', content: 'text/html; charset=utf-8' },
          { name: 'language', content: 'English' },
          { name: 'author', content: 'Gabber235, Marten_mrfcyt' },
          { name: 'og:type', content: 'website' },
          { name: 'twitter:card', content: 'app' },
          { name: 'twitter:url', content: 'https://docs.typewritermc.com/' },
      ],
      // Replace with your project's social card
      image: 'img/typewriter.png',
      navbar: {
          title: 'TypeWriter',
          logo: {
              alt: 'TypeWriter Logo',
              src: 'img/typewriter.png',
          },
          items: [
              {
                  type: 'doc',
                  docId: 'docs/home',
                  position: 'left',
                  label: 'Documentation',
              },
              {
                  type: 'docSidebar',
                  label: 'Extensions',
                  sidebarId: 'adapters',
                  position: 'left',
              },
              {
                  type: 'docSidebar',
                  sidebarId: 'develop',
                  position: 'left',
                  label: 'Develop',
              },
              { to: '/devlog', label: 'Devlog', position: 'left' },
              {
                  type: 'docsVersionDropdown',
                  position: 'right',
              },
              {
                  href: 'https://github.com/gabber235/TypeWriter',
                  label: 'GitHub',
                  position: 'right',
              },
          ],
      },
      footer: {
          style: 'dark',
          links: [
              {
                  title: 'Information',
                  items: [
                      {
                          label: 'Documentation',
                          to: '/docs/home',
                      },
                      {
                          label: 'Clickup',
                          href: 'https://sharing.clickup.com/9015308602/l/h/6-901502296591-1/e32ea9f33a22632',
                      },
                      {
                          label: 'Privacy Policy',
                          to: '/privacy-policy',
                      }

                  ],
              },
              {
                  title: 'Community',
                  items: [
                      {
                          label: 'Discord',
                          href: 'https://discord.gg/gs5QYhfv9x',
                      },

                  ],
              },
              {
                  title: 'More',
                  items: [
                      {
                          label: 'Devlog',
                          to: '/devlog',
                      },
                      {
                          label: 'Github',
                          href: 'https://github.com/gabber235/TypeWriter',
                      },
                  ],
              },
          ],
          copyright: `Copyright © ${new Date().getFullYear()} Typewriter`,
      },
      prism: {
          theme: lightTheme,
          darkTheme: darkTheme,
          additionalLanguages: ['kotlin', 'yaml', 'java', 'log'],
          magicComments: [
              {
                  className: 'highlight-red',
                  line: 'highlight-red', // For single line
                  block: { start: 'highlight-red-start', end: 'highlight-red-end' }, // For block
              },
              {
                  className: 'highlight-green',
                  line: 'highlight-green', // For single line
                  block: { start: 'highlight-green-start', end: 'highlight-green-end' }, // For block
              },
              {
                  className: 'highlight-blue',
                  line: 'highlight-blue', // For single line
                  block: { start: 'highlight-blue-start', end: 'highlight-blue-end' }, // For block
              },
              {
                  className: 'highlight-line',
                  line: 'highlight-next-line', // For single line
                  block: { start: 'highlight-start', end: 'highlight-end' }, // For block
              },
          ]
      },
      algolia: {
        appId: 'GE6F02MN59',
        apiKey: '57ae467d6c3f66ac2cae2c98e4275f49',
        indexName: 'typewriter',
        contextualSearch: true,
    },
  }),
future: {
    experimental_faster: {
        swcJsLoader: true,
        swcJsMinimizer: true,
        swcHtmlMinimizer: true,
        lightningCssMinimizer: true,
        rspackBundler: false,
        mdxCrossCompilerCache: true,
      },
},
};

export default config;