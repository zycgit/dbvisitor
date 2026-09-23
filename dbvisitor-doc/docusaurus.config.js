// @ts-check
// Note: type annotations allow type checking and IDEs autocompletion

const {themes} = require('prism-react-renderer');
const lightCodeTheme = themes.github;
const darkCodeTheme = themes.dracula;
const analyticsPlugin = require('./plugins/analytics.js');
const projectVars = require('./plugins/projectVars.js');
const remarkProjectVars = require('./plugins/remark-project-vars.js');
const {GlobExcludeDefault} = require('@docusaurus/utils');

/** @type {import('@docusaurus/types').Config} */
const config = {
    title: 'dbVisitor - Java 数据库开发工具',
    tagline: 'dbVisitor Java 数据库开发工具',
    url: 'https://www.dbvisitor.net',
    baseUrl: '/',
    onBrokenLinks: 'throw',
    markdown: {
        preprocessor: ({fileContent}) => remarkProjectVars.replaceVariables(fileContent, projectVars),
        parseFrontMatter: ({filePath, fileContent, defaultParseFrontMatter}) => defaultParseFrontMatter({
            filePath,
            fileContent: remarkProjectVars.replaceVariables(fileContent, projectVars),
        }),
    },
    favicon: 'img/favicon/crab.ico',
    organizationName: 'zycgit', // Usually your GitHub org/user name.
    projectName: 'dbVisitor',   // Usually your repo name.
    i18n: {
        defaultLocale: 'zh-cn',
        locales: ['zh-cn', 'en'],
    },
    presets: [
        [
            'classic',
            /** @type {import('@docusaurus/preset-classic').Options} */
            {
                docs: {
                    remarkPlugins: [[remarkProjectVars, projectVars]],
                    sidebarPath: require.resolve('./sidebars.js'),
                    async sidebarItemsGenerator({defaultSidebarItemsGenerator, ...args}) {
                        const items = await defaultSidebarItemsGenerator(args);
                        // The overview is the landing page of the differences category.
                        return args.item.dirName === 'features'
                            ? items.filter(item => !(item.type === 'doc' && item.id === 'features/overview'))
                            : items;
                    },
                    editUrl: 'https://gitee.com/zycgit/dbvisitor/blob/main/dbvisitor-doc/',
                },
                blog: {
                    exclude: [...GlobExcludeDefault, '**/assets/**'],
                    remarkPlugins: [[remarkProjectVars, projectVars]],
                    showReadingTime: true,
                    blogSidebarCount: 25,
                    postsPerPage: 10,
                    feedOptions: {
                        type: ['rss', 'atom'],
                        xslt: true,
                    },
                    editUrl: 'https://gitee.com/zycgit/dbvisitor/blob/main/dbvisitor-doc/',
                    onInlineTags: 'warn',
                    onInlineAuthors: 'warn',
                    onUntruncatedBlogPosts: 'warn',
                },
                theme: {
                    customCss: require.resolve('./src/css/custom.css'),
                },
            }
        ],
    ],
    themeConfig: /** @type {import('@docusaurus/preset-classic').ThemeConfig} */ {
        metadata: [
            {
                name: 'keywords',
                content: 'orm,mybatis,mybatis plus,dbvisitor,jooq,spring,springboot,spring框架,jdbc,jdbctemplate,spring orm,开源,开源软件,java开源,开源项目,开源代码'
            },
            {
                name: 'description',
                content: 'dbVisitor 是一个轻量小巧的数据库 ORM 工具，提供ORM、丰富的TypeHandler、动态SQL、存储过程、 内置分页方言20+、支持嵌套事务、多数据源、条件构造器、INSERT 策略、多语句/多结果。兼容 Spring 及 MyBatis 用法。'
            }
        ],
        colorMode: {
            disableSwitch: true,
        },
        navbar: {
            logo: {
                alt: 'dbVisitor Logo',
                src: 'img/favicon/crab.png',
            },
            items: [
                {
                    type: 'docSidebar',
                    sidebarId: 'guides',
                    position: 'left',
                    label: '核心API',
                },
                {
                    type: 'docSidebar',
                    sidebarId: 'features',
                    position: 'left',
                    label: '数据源',
                },
                {
                    type: 'docSidebar',
                    sidebarId: 'drivers',
                    position: 'left',
                    label: 'JDBC 驱动',
                },
                {
                    type: 'doc',
                    docId: 'releases/latest',
                    position: 'left',
                    label: '版本记录',
                },
                {
                    to: '/blog/archive',
                    activeBasePath: '/blog',
                    label: '博客',
                    position: 'left'
                },
                {
                    position: 'right',
                    label: '码云',
                    href: 'https://gitee.com/zycgit/dbvisitor'
                },
                {
                    position: 'right',
                    label: 'Github',
                    href: 'https://github.com/zycgit/dbvisitor'
                },
                {
                    type: 'localeDropdown',
                    position: 'right',
                }
            ]
        },
        prism: {
            theme: lightCodeTheme,
            darkTheme: darkCodeTheme,
            additionalLanguages: ['java']
        },
        footer: {
            style: 'dark',
            copyright: `Copyright © ${new Date().getFullYear()} dbVisitor. Built with Docusaurus.<br/>
<a target="_blank" href="http://www.beian.gov.cn/portal/registerSystemInfo?recordcode=33011002016667">
<img src="/img/beian.png" style="display: inline-block;">浙公网安备 33011002016667号
</a>&nbsp;&nbsp;<a target="_blank" href="https://beian.miit.gov.cn/#/Integrated/index">浙ICP备18034797号-5</a>
<div id="analyticsDiv" style="display: inline-block;"></div>`,
        },
    },
    plugins: [
        analyticsPlugin,
        require.resolve('./plugins/blog-topics.js'),
        [
            require.resolve('./plugins/llms.js'),
            {
                siteTitle: 'dbVisitor',
                overview: 'guides/overview.mdx',
                descriptions: {
                    'zh-cn': `dbVisitor ${projectVars.docsVersion} 文档：Java 数据库 API、数据源特性、JDBC 驱动和实战教程。开发版本在版本说明中单独标注。`,
                    en: `dbVisitor ${projectVars.docsVersion} documentation: Java database APIs, data source capabilities, JDBC drivers, and tutorials. Development releases are marked separately.`,
                },
                depth: 2,
                onRouteError: 'throw',
                content: {
                    enableMarkdownFiles: false,
                    enableLlmsFullTxt: false,
                    includeDocs: true,
                    includeBlog: true,
                    includePages: false,
                    includeGeneratedIndex: false,
                    excludeRoutes: [
                        '**/tags{,/**}', '**/search', '**/404.html',
                        '**/blog', '**/blog/{archive,authors,page,topics}{,/**}',
                    ],
                    contentSelectors: ['.theme-doc-markdown', 'article'],
                },
            },
        ],
    ],
    themes: [
        // ... Your other themes.
        [
            require.resolve("@easyops-cn/docusaurus-search-local"),
            /** @type {import("@easyops-cn/docusaurus-search-local").PluginOptions} */
            ({
                // ... Your options.
                // `hashed` is recommended as long-term-cache of index file is possible.
                hashed: true,
                // For Docs using Chinese, The `language` is recommended to set to:
                language: ["en", "zh"],
            }),
        ],
    ]
};

module.exports = config;
