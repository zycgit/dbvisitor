// @ts-check
// Note: type annotations allow type checking and IDEs autocompletion

const {themes} = require('prism-react-renderer');
const lightCodeTheme = themes.github;
const darkCodeTheme = themes.dracula;
const analyticsPlugin = require('./plugins/analytics.js');

/** @type {import('@docusaurus/types').Config} */
const config = {
    title: 'dbVisitor - Java 数据库开发工具',
    tagline: 'dbVisitor Java 数据库开发工具',
    url: 'https://www.dbvisitor.net',
    baseUrl: '/',
    onBrokenLinks: 'throw',
    favicon: 'img/favicon.ico',
    organizationName: 'zycgit', // Usually your GitHub org/user name.
    projectName: 'dbVisitor',   // Usually your repo name.
    i18n: {
        defaultLocale: 'en',
        locales: ['zh-cn', 'en'],
    },
    presets: [
        [
            'classic',
            /** @type {import('@docusaurus/preset-classic').Options} */
            {
                docs: {
                    sidebarPath: require.resolve('./sidebars.js'),
                    editUrl: 'https://gitee.com/zycgit/dbvisitor/blob/main/dbvisitor-doc/',
                },
                blog: {
                    showReadingTime: true,
                    blogSidebarCount: 10,
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
                src: 'img/logo.svg',
            },
            items: [
                {
                    type: 'docSidebar',
                    sidebarId: 'guides',
                    position: 'left',
                    label: '数据库访问',
                },
                {
                    type: 'docSidebar',
                    sidebarId: 'drivers',
                    position: 'left',
                    label: '驱动适配器',
                },
                {
                    type: 'doc',
                    docId: 'releases/latest',
                    position: 'left',
                    label: '版本记录',
                },
                {
                    to: '/blog',
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
        analyticsPlugin
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
