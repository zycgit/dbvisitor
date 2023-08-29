// @ts-check
// Note: type annotations allow type checking and IDEs autocompletion

const lightCodeTheme = require('prism-react-renderer/themes/github');
const darkCodeTheme = require('prism-react-renderer/themes/dracula');
const analyticsPlugin = require('./plugins/analytics.js');

/** @type {import('@docusaurus/types').Config} */
const config = {
    title: 'dbVisitor - Java 数据库开发工具',
    tagline: 'dbVisitor Java 数据库开发工具',
    url: 'https://www.dbvisitor.net',
    baseUrl: '/',
    onBrokenLinks: 'throw',
    onBrokenMarkdownLinks: 'warn',
    favicon: 'img/favicon.ico',
    organizationName: 'zycgit', // Usually your GitHub org/user name.
    projectName: 'dbvisitor',   // Usually your repo name.

    i18n: {
        defaultLocale: 'zh-cn',
        locales: ['zh-cn', 'en'],
    },

    presets: [
        [
            'classic',
            /** @type {import('@docusaurus/preset-classic').Options} */
            ({
                docs: {
                    sidebarPath: require.resolve('./sidebars.js'),
                    editUrl: 'https://gitee.com/zycgit/dbvisitor/tree/master/dbvisitor-doc',
                },
                theme: {
                    customCss: require.resolve('./src/css/custom.css'),
                },
            }),
        ],
    ],

    themeConfig: /** @type {import('@docusaurus/preset-classic').ThemeConfig} */ {
        metadata: [
            {name: 'keywords', content: 'orm,mybatis,mybatis plus,dbvisitor,jooq,spring,springboot,spring框架,jdbc,jdbctemplate,spring orm,开源,开源软件,java开源,开源项目,开源代码'},
            {name: 'description', content: 'dbVisitor 是一个轻量小巧的数据库 ORM 工具，提供ORM、丰富的TypeHandler、动态SQL、存储过程、 内置分页方言20+、支持嵌套事务、多数据源、条件构造器、INSERT 策略、多语句/多结果。兼容 Spring 及 MyBatis 用法。'}
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
                    position: 'left',
                    label: 'dbVisitor 框架',
                    href: '/dbvisitor'
                },
                {
                    type: 'doc',
                    docId: 'releases/latest',
                    position: 'left',
                    label: '发布版本',
                },
                {
                    position: 'right',
                    label: 'QQ群 948706820',
                    href: 'https://qm.qq.com/cgi-bin/qm/qr?k=FpO8j2y59N7gGEvX7DOuB8Hx79lE425-&jump_from=webapi&authKey=QF7WNIwxzH6ZsVlCpJlcyFId++YPovIhI8TvuV8/L6x7Icf1FSqYGI/svlUZyF4R'
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
        [
            require.resolve("@cmfcmf/docusaurus-search-local"),
            {
                indexPages: true,
                // When applying `zh` in language, please install `nodejieba` in your project.
                language: ["en", "zh"],
                maxSearchResults: 8
            }
        ]
    ]
};

module.exports = config;
