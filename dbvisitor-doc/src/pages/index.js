import React, {useState, useEffect} from 'react';
import clsx from 'clsx';
import Layout from '@theme/Layout';
import Link from '@docusaurus/Link';
import Translate, {translate} from '@docusaurus/Translate';
import useDocusaurusContext from '@docusaurus/useDocusaurusContext';
import styles from './index.module.css';
import Vars from '@site/plugins/projectVars';

/* ==================== Static Data ==================== */

const LabelList = [
    {
        alt: 'License',
        Svg: require('../../static/img/labels/license.svg').default,
        href: 'https://www.apache.org/licenses/LICENSE-2.0.html'
    },
    {
        alt: 'Email',
        Svg: require('../../static/img/labels/email-zyc@byshell.svg').default,
        href: 'mailto:zyc@byshell.org'
    },
    {
        alt: 'QQ Group',
        Svg: require('../../static/img/labels/qqgroup-948706820.svg').default,
        href: 'https://qm.qq.com/cgi-bin/qm/qr?k=-ha3lrkHrAddrZMINYbmxj4W7ZrrWW2b&jump_from=webapi&authKey=BtyfWIjWF7uhOf/ZPur+pr5p1efOZyjGESLynkPzbJ9IMd/j/T/pR1SDLcJKC972'
    },
];

const RdbmsIcons = [
    {name: 'MySQL', icon: require('../../static/img/ds_icons/mysql.svg').default},
    {name: 'PostgreSQL', icon: require('../../static/img/ds_icons/postgresql.svg').default},
    {name: 'Oracle', icon: require('../../static/img/ds_icons/oracle.svg').default},
    {name: 'SQL Server', icon: require('../../static/img/ds_icons/sqlserver.svg').default},
    {name: 'DB2', icon: require('../../static/img/ds_icons/ibmdb2.svg').default},
    {name: 'ClickHouse', icon: require('../../static/img/ds_icons/clickhouse.svg').default},
    {name: 'TiDB', icon: require('../../static/img/ds_icons/tidb.svg').default},
    {name: 'OceanBase', icon: require('../../static/img/ds_icons/oceanbase.svg').default},
    {name: 'DM', icon: require('../../static/img/ds_icons/dm.svg').default},
    {name: 'StarRocks', icon: require('../../static/img/ds_icons/starrocks.svg').default},
    {name: 'Doris', icon: require('../../static/img/ds_icons/doris.svg').default},
];

const NosqlIcons = [
    {name: 'Redis', icon: require('../../static/img/ds_icons/redis.svg').default},
    {name: 'MongoDB', icon: require('../../static/img/ds_icons/mongodb.svg').default},
    {name: 'Elasticsearch', icon: require('../../static/img/ds_icons/elastic.svg').default},
    {name: 'Milvus', icon: require('../../static/img/ds_icons/milvus.svg').default},
];

/* ==================== Hero Section ==================== */

function HeroSection() {
    return (
        <header className={styles.heroBanner}>
            <div className="container">
                <h1 className={styles.heroTitle}>dbVisitor</h1>
                <p className={styles.heroSlogan}>One APIs Access Any DataBase</p>
                <p className={styles.heroDesc}>
                    <Translate id="dbv.hero.desc">
                        承认差异、管理差异、而非消灭差异 —— 通过双层适配架构，通过统一 API 访问已支持的数据库。
                    </Translate>
                </p>
                <div className={styles.heroBadges}>
                    {LabelList.map((item, idx) => (
                        <a key={idx} target="_blank" rel="noopener noreferrer" href={item.href}>
                            <item.Svg alt={item.alt}/>
                        </a>
                    ))}
                </div>
                <div className={styles.heroButtons}>
                    <Link className={clsx('button button--lg', styles.btnPrimary)}
                          to="/docs/guides/overview">
                        <Translate id="dbv.hero.getStarted">快速开始</Translate>
                    </Link>
                    <Link className={clsx('button button--lg', styles.btnSecondary)}
                          to="https://github.com/zycgit/dbvisitor">
                        GitHub
                    </Link>
                </div>
            </div>
        </header>
    );
}

/* ==================== Database Support Section ==================== */

function DatabaseSection() {
    return (
        <section className={styles.dbSection}>
            <div className="container">
                <h2 className={styles.sectionTitle}>
                    <Translate id="dbv.db.title">广泛的数据库支持</Translate>
                </h2>
                <p className={styles.sectionSubtitle}>
                    <Translate id="dbv.db.subtitle">
                        覆盖多种关系型数据库方言，并通过 JDBC 驱动适配器支持 NoSQL 和向量数据库
                    </Translate>
                </p>
                <div className={styles.dbGrid}>
                    {RdbmsIcons.map((db, idx) => (
                        <div key={idx} className={styles.dbItem}>
                            <db.icon className={styles.dbIcon}/>
                            <span className={styles.dbLabel}>{db.name}</span>
                        </div>
                    ))}
                    <div className={styles.dbDivider}/>
                    {NosqlIcons.map((db, idx) => (
                        <div key={idx} className={styles.dbItem}>
                            <db.icon className={styles.dbIcon}/>
                            <span className={styles.dbLabel}>{db.name}</span>
                        </div>
                    ))}
                </div>
            </div>
        </section>
    );
}

/* ==================== Core Philosophy Section ==================== */

function PhilosophySection() {
    const features = [
        {
            icon: '🔗',
            title: translate({id: 'dbv.phil.unified.title', message: '统一 APIs'}),
            desc: translate({
                id: 'dbv.phil.unified.desc',
                message: 'JdbcTemplate、声明式接口、BaseMapper、LambdaTemplate 和 XML Mapper',
            }),
        },
        {
            icon: '📐',
            title: translate({id: 'dbv.phil.adapter.title', message: '双层适配架构'}),
            desc: translate({
                id: 'dbv.phil.adapter.desc',
                message: '应用访问层、标准驱动层、独立演进、独立使用',
            }),
        },
        {
            icon: '🧩',
            title: translate({id: 'dbv.phil.zero.title', message: '零耦合'}),
            desc: translate({
                id: 'dbv.phil.zero.desc',
                message: '可复杂、可简单、自由集成、任意使用',
            }),
        },
    ];

    return (
        <section className={styles.philosophySection}>
            <div className="container">
                <h2 className={styles.sectionTitle} style={{textAlign: 'center'}}>
                    <Translate id="dbv.phil.title">核心理念</Translate>
                </h2>
                <p className={styles.sectionSubtitle} style={{textAlign: 'center'}}>
                    <Translate id="dbv.phil.subtitle">使用不同层面的 API 来解决不同层面的问题，尊重数据源差异，管理数据源差异。</Translate>
                </p>
                <div className={styles.featureGrid}>
                    {features.map((f, idx) => (
                        <div key={idx} className={styles.featureCard}>
                            <div className={styles.featureIcon}>{f.icon}</div>
                            <div className={styles.featureTitle}>{f.title}</div>
                            <div className={styles.featureDesc}>{f.desc}</div>
                        </div>
                    ))}
                </div>
            </div>
        </section>
    );
}

/* ==================== Architecture Section ==================== */

function ArchitectureSection() {
    return (
        <section className={styles.archSection}>
            <div className="container">
                <h2 className={styles.sectionTitle} style={{textAlign: 'center'}}>
                    <Translate id="dbv.arch.title">双层适配架构</Translate>
                </h2>
                <p className={styles.sectionSubtitle} style={{textAlign: 'center'}}>
                    <Translate id="dbv.arch.subtitle">
                        应用层管理查询方式差异，协议层管理通信协议差异
                    </Translate>
                </p>
                <div className={styles.archDiagram}>
                    <img
                        src="/img/double.png"
                        alt="Dual-Layer Adapter Architecture"
                        className={styles.archImage}
                    />
                </div>
            </div>
        </section>
    );
}

/* ==================== 5 API Styles Carousel Section ==================== */

function CodeBlock({title, children}) {
    return (
        <div className={styles.codeBlock}>
            <div className={styles.codeHeader}>
                <span className={styles.codeTitle}>{title}</span>
            </div>
            <pre className={styles.codeContent}>{children}</pre>
        </div>
    );
}

function ApiStylesSection() {
    const [activeIdx, setActiveIdx] = useState(0);
    const [paused, setPaused] = useState(false);

    const apiStyles = [
        {
            icon: '⌨️',
            name: 'JdbcTemplate',
            desc: translate({id: 'dbv.api.jdbc.desc', message: '原生 SQL 操作，最直接的数据库访问方式'}),
            docLink: '/docs/guides/api/jdbc',
            codeTitle: 'JdbcTemplate.java',
            code: (s) => (<>
                <span className={s.codeType}>{'JdbcTemplate'}</span>{' jdbc = '}<span className={s.codeKeyword}>{'new'}</span>{' '}<span className={s.codeType}>{'JdbcTemplate'}</span>{'(dataSource);\n\n'}
                <span className={s.codeComment}>{translate({id: 'dbv.api.jdbc.c1', message: '// 查询映射到 Bean'}) + '\n'}</span>
                <span className={s.codeType}>{'List'}</span>{'<'}<span className={s.codeType}>{'User'}</span>{'>'}{' users = jdbc.'}<span className={s.codeMethod}>{'queryForList'}</span>{'(\n'}
                {'    '}<span className={s.codeString}>{'"select * from users where age > ?"'}</span>{',\n'}
                {'    '}<span className={s.codeKeyword}>{'new'}</span>{' '}<span className={s.codeType}>{'Object'}</span>{'[] { '}<span className={s.codeString}>{'18'}</span>{' },\n'}
                {'    '}<span className={s.codeType}>{'User'}</span>{'.class\n);\n\n'}
                <span className={s.codeComment}>{translate({id: 'dbv.api.jdbc.c2', message: '// 查询单值'}) + '\n'}</span>
                <span className={s.codeType}>{'Long'}</span>{' total = jdbc.'}<span className={s.codeMethod}>{'queryForObject'}</span>{'(\n'}
                {'    '}<span className={s.codeString}>{'"select count(*) from users"'}</span>{',\n'}
                {'    '}<span className={s.codeType}>{'Long'}</span>{'.class\n);\n'}
            </>),
        },
        {
            icon: '📝',
            name: translate({id: 'dbv.api.annotation.name', message: '声明式接口'}),
            desc: translate({id: 'dbv.api.annotation.desc', message: '声明式接口 + 注解，类似 MyBatis Mapper'}),
            docLink: '/docs/guides/api/mapper',
            codeTitle: 'UserMapper.java',
            code: (s) => (<>
                <span className={s.codeKeyword}>{'@SimpleMapper'}</span>{'\n'}
                <span className={s.codeKeyword}>{'public interface'}</span>{' '}<span className={s.codeType}>{'UserMapper'}</span>{' {\n'}
                {'    '}<span className={s.codeKeyword}>{'@Query'}</span>{'('}<span className={s.codeString}>{'"select * from users where id = #{id}"'}</span>{')\n'}
                {'    '}<span className={s.codeType}>{'User'}</span>{' '}<span className={s.codeMethod}>{'selectById'}</span>{'('}<span className={s.codeKeyword}>{'@Param'}</span>{'('}<span className={s.codeString}>{'"id"'}</span>{') '}<span className={s.codeType}>{'int'}</span>{' id);\n\n'}
                {'    '}<span className={s.codeKeyword}>{'@Insert'}</span>{'('}<span className={s.codeString}>{'"insert into users (name, age) values (#{name}, #{age})"'}</span>{')\n'}
                {'    '}<span className={s.codeType}>{'int'}</span>{' '}<span className={s.codeMethod}>{'insertUser'}</span>{'('}
                            <span className={s.codeKeyword}>{'@Param'}</span>{'('}<span className={s.codeString}>{'"name"'}</span>{') '}<span className={s.codeType}>{'String'}</span>{' name, '}
                            <span className={s.codeKeyword}>{'@Param'}</span>{'('}<span className={s.codeString}>{'"age"'}</span>{')  '}<span className={s.codeType}>{'int'}</span>{' age);\n}\n'}
            </>),
        },
        {
            icon: '🧱',
            name: translate({id: 'dbv.api.base.name', message: '通用 Mapper'}),
            desc: translate({id: 'dbv.api.base.desc', message: '通用 CRUD 操作，零 SQL 快速开发'}),
            docLink: '/docs/guides/core/mapper/about',
            codeTitle: 'BaseMapper.java',
            code: (s) => (<>
                <span className={s.codeType}>{'BaseMapper'}</span>{'<'}<span className={s.codeType}>{'User'}</span>{'>'}{' mapper = session.'}<span className={s.codeMethod}>{'createBaseMapper'}</span>{'('}<span className={s.codeType}>{'User'}</span>{'.class);\n\n'}
                <span className={s.codeComment}>{translate({id: 'dbv.api.base.c1', message: '// 主键查询'}) + '\n'}</span>
                <span className={s.codeType}>{'User'}</span>{' user = mapper.'}<span className={s.codeMethod}>{'selectById'}</span>{'('}<span className={s.codeString}>{'1'}</span>{');\n\n'}
                <span className={s.codeComment}>{translate({id: 'dbv.api.base.c2', message: '// 更新已存在的用户'}) + '\n'}</span>
                {'if (user != null) {\n    user.'}<span className={s.codeMethod}>{'setAge'}</span>{'('}<span className={s.codeString}>{'30'}</span>{');\n'}
                {'    mapper.'}<span className={s.codeMethod}>{'update'}</span>{'(user);\n}\n\n'}
                <span className={s.codeComment}>{translate({id: 'dbv.api.base.c3', message: '// 主键删除'}) + '\n'}</span>
                {'mapper.'}<span className={s.codeMethod}>{'deleteById'}</span>{'('}<span className={s.codeString}>{'1'}</span>{');\n'}
            </>),
        },
        {
            icon: '🔮',
            name: translate({id: 'dbv.api.lambda.name', message: '条件构造器'}),
            desc: translate({id: 'dbv.api.lambda.desc', message: 'Lambda 表达式构建查询，类型安全'}),
            docLink: '/docs/guides/api/lambda',
            codeTitle: 'LambdaTemplate.java',
            code: (s) => (<>
                <span className={s.codeType}>{'LambdaTemplate'}</span>{' lambda = '}<span className={s.codeKeyword}>{'new'}</span>{' '}<span className={s.codeType}>{'LambdaTemplate'}</span>{'(dataSource);\n\n'}
                <span className={s.codeComment}>{translate({id: 'dbv.api.lambda.c1', message: '// 类型安全的 Lambda 查询'}) + '\n'}</span>
                <span className={s.codeType}>{'List'}</span>{'<'}<span className={s.codeType}>{'User'}</span>{'>'}{' users = lambda\n'}
                {'    .'}<span className={s.codeMethod}>{'query'}</span>{'('}<span className={s.codeType}>{'User'}</span>{'.class)\n'}
                {'    .'}<span className={s.codeMethod}>{'eq'}</span>{'('}<span className={s.codeType}>{'User'}</span>{'::'}<span className={s.codeMethod}>{'getName'}</span>{', '}<span className={s.codeString}>{'"Alice"'}</span>{')\n'}
                {'    .'}<span className={s.codeMethod}>{'ge'}</span>{'('}<span className={s.codeType}>{'User'}</span>{'::'}<span className={s.codeMethod}>{'getAge'}</span>{', '}<span className={s.codeString}>{'20'}</span>{')\n'}
                {'    .'}<span className={s.codeMethod}>{'queryForList'}</span>{'();\n'}
            </>),
        },
        {
            icon: '📄',
            name: 'XML Mapper',
            desc: translate({id: 'dbv.api.xml.desc', message: 'XML 定义 SQL 映射，兼容 MyBatis 风格'}),
            docLink: '/docs/guides/api/file_mapper',
            codeTitle: 'userMapper.xml + UserMapper.java',
            code: (s) => (<>
                <span className={s.codeComment}>{translate({id: 'dbv.api.xml.c1', message: '// XML 定义（userMapper.xml）'}) + '\n'}</span>
                <span className={s.codeKeyword}>{'<select'}</span>{' '}<span className={s.codeType}>{'id'}</span>{'='}<span className={s.codeString}>{'"selectById"'}</span>{' '}<span className={s.codeType}>{'resultType'}</span>{'='}<span className={s.codeString}>{'"com.example.User"'}</span><span className={s.codeKeyword}>{'>'}</span>{'\n'}
                {'  '}<span className={s.codeString}>{'select * from users where id = #{id}'}</span>{'\n'}
                <span className={s.codeKeyword}>{'</select>'}</span>{'\n\n'}
                <span className={s.codeComment}>{translate({id: 'dbv.api.xml.c2', message: '// Java 接口'}) + '\n'}</span>
                <span className={s.codeKeyword}>{'@RefMapper'}</span>{'('}<span className={s.codeString}>{'"userMapper.xml"'}</span>{')\n'}
                <span className={s.codeKeyword}>{'public interface'}</span>{' '}<span className={s.codeType}>{'UserMapper'}</span>{' {\n'}
                {'    '}<span className={s.codeType}>{'User'}</span>{' '}<span className={s.codeMethod}>{'selectById'}</span>{'('}<span className={s.codeKeyword}>{'@Param'}</span>{'('}<span className={s.codeString}>{'"id"'}</span>{') '}<span className={s.codeType}>{'int'}</span>{' id);\n}\n'}
            </>),
        },
    ];

    // Auto-rotate every 5 seconds
    useEffect(() => {
        if (paused) {
            return;
        }
        const timer = setInterval(() => {
            setActiveIdx((prev) => (prev + 1) % apiStyles.length);
        }, 5000);
        return () => clearInterval(timer);
    }, [paused, apiStyles.length]);

    const current = apiStyles[activeIdx];

    return (
        <section className={styles.apiSection}>
            <div className="container">
                <h2 className={styles.sectionTitle} style={{textAlign: 'center'}}>
                    <Translate id="dbv.api.title">5 种 API 风格</Translate>
                </h2>
                <p className={styles.sectionSubtitle} style={{textAlign: 'center'}}>
                    <Translate id="dbv.api.subtitle">
                        选择最适合场景的 API，所有风格共享同一套底层运行机制。
                    </Translate>
                </p>

                {/* Tab buttons */}
                <div className={styles.apiTabs}
                     onMouseLeave={() => setPaused(false)}>
                    {apiStyles.map((api, idx) => (
                        <button
                            key={idx}
                            className={clsx(styles.apiTab, idx === activeIdx && styles.apiTabActive)}
                            onMouseEnter={() => { setActiveIdx(idx); setPaused(true); }}
                            onClick={() => { setActiveIdx(idx); }}
                        >
                            <span className={styles.apiTabIcon}>{api.icon}</span>
                            <span className={styles.apiTabName}>{api.name}</span>
                        </button>
                    ))}
                </div>

                {/* Progress bar */}
                <div className={styles.apiProgress}>
                    {apiStyles.map((_, idx) => (
                        <div key={idx} className={clsx(styles.apiProgressDot, idx === activeIdx && styles.apiProgressDotActive)}/>
                    ))}
                </div>

                {/* Content: description + code */}
                <div className={styles.apiShowcase}
                     onMouseEnter={() => setPaused(true)}
                     onMouseLeave={() => setPaused(false)}>
                    <div className={styles.apiInfo}>
                        <div className={styles.apiInfoIcon}>{current.icon}</div>
                        <h3 className={styles.apiInfoTitle}>{current.name}</h3>
                        <p className={styles.apiInfoDesc}>{current.desc}</p>
                        <Link className="button button--primary button--sm" to={current.docLink}>
                            <Translate id="dbv.api.viewDoc">查看文档</Translate> →
                        </Link>
                    </div>
                    <div className={styles.apiCode}>
                        <CodeBlock title={current.codeTitle}>
                            {current.code(styles)}
                        </CodeBlock>
                    </div>
                </div>
            </div>
        </section>
    );
}

/* ==================== Code Example Section (One APIs, Any DB) ==================== */

function CodeExampleSection() {
    return (
        <section className={styles.codeSection}>
            <div className="container">
                <h2 className={styles.sectionTitle} style={{textAlign: 'center'}}>
                    <Translate id="dbv.code.title">同一套 API，不同的数据库</Translate>
                </h2>
                <p className={styles.sectionSubtitle} style={{textAlign: 'center'}}>
                    <Translate id="dbv.code.subtitle">
                        在驱动支持的范围内复用 API，SQL、字段映射和事务能力仍取决于数据源
                    </Translate>
                </p>
                <div className={styles.codeContainer}>
                    <CodeBlock title="MySQL / PostgreSQL">
                        <span className={styles.codeComment}>{translate({id: 'dbv.code.comment.rdb', message: '// 连接关系型数据库'}) + '\n'}</span>
                        <span className={styles.codeType}>{'Configuration'}</span>{' config = '}<span className={styles.codeKeyword}>{'new'}</span>{' '}<span className={styles.codeType}>{'Configuration'}</span>{'();\n'}
                        <span className={styles.codeType}>{'LambdaTemplate'}</span>{' t = config.'}<span className={styles.codeMethod}>{'newLambda'}</span>{'(ds);\n\n'}
                        <span className={styles.codeComment}>{translate({id: 'dbv.code.comment.lambda', message: '// Lambda 查询'}) + '\n'}</span>
                        <span className={styles.codeType}>{'List'}</span>{'<'}<span className={styles.codeType}>{'User'}</span>{'>'}{' users = t\n'}
                        {'    .'}<span className={styles.codeMethod}>{'query'}</span>{'('}<span className={styles.codeType}>{'User'}</span>{'.class)\n'}
                        {'    .'}<span className={styles.codeMethod}>{'eq'}</span>{'('}<span className={styles.codeType}>{'User'}</span>{'::'}<span className={styles.codeMethod}>{'getAge'}</span>{', '}<span className={styles.codeString}>{'18'}</span>{')\n'}
                        {'    .'}<span className={styles.codeMethod}>{'queryForList'}</span>{'();\n'}
                    </CodeBlock>

                    <CodeBlock title="MongoDB / Elasticsearch">
                        <span className={styles.codeComment}>{translate({id: 'dbv.code.comment.nosql', message: '// 连接 NoSQL 数据库（同一套 API）'}) + '\n'}</span>
                        <span className={styles.codeType}>{'Configuration'}</span>{' config = '}<span className={styles.codeKeyword}>{'new'}</span>{' '}<span className={styles.codeType}>{'Configuration'}</span>{'();\n'}
                        <span className={styles.codeType}>{'LambdaTemplate'}</span>{' t = config.'}<span className={styles.codeMethod}>{'newLambda'}</span>{'(ds);\n\n'}
                        <span className={styles.codeComment}>{translate({id: 'dbv.code.comment.same', message: '// 完全相同的 Lambda 查询'}) + '\n'}</span>
                        <span className={styles.codeType}>{'List'}</span>{'<'}<span className={styles.codeType}>{'User'}</span>{'>'}{' users = t\n'}
                        {'    .'}<span className={styles.codeMethod}>{'query'}</span>{'('}<span className={styles.codeType}>{'User'}</span>{'.class)\n'}
                        {'    .'}<span className={styles.codeMethod}>{'eq'}</span>{'('}<span className={styles.codeType}>{'User'}</span>{'::'}<span className={styles.codeMethod}>{'getAge'}</span>{', '}<span className={styles.codeString}>{'18'}</span>{')\n'}
                        {'    .'}<span className={styles.codeMethod}>{'queryForList'}</span>{'();\n'}
                    </CodeBlock>
                </div>
            </div>
        </section>
    );
}

/* ==================== Quick Start Section ==================== */

function QuickStartSection() {
    return (
        <section className={styles.quickStartSection}>
            <div className="container">
                <h2 className={styles.sectionTitle} style={{textAlign: 'center'}}>
                    <Translate id="dbv.start.title">快速开始</Translate>
                </h2>
                <p className={styles.sectionSubtitle} style={{textAlign: 'center'}}>
                    <Translate id="dbv.start.subtitle">
                        引入核心库与目标 JDBC 驱动，以下使用最新正式版
                    </Translate>
                </p>
                <div className={styles.quickStartContainer}>
                    <div className={styles.mavenBlock}>
                        <div className={styles.codeHeader}>
                            <span className={styles.codeTitle}>pom.xml</span>
                        </div>
                        <pre className={styles.codeContent}>
                            {'<'}<span className={styles.codeKeyword}>{'dependency'}</span>{'>\n'}
                            {'  <'}<span className={styles.codeKeyword}>{'groupId'}</span>{'>'}<span className={styles.codeString}>{'net.hasor'}</span>{'</'}<span className={styles.codeKeyword}>{'groupId'}</span>{'>\n'}
                            {'  <'}<span className={styles.codeKeyword}>{'artifactId'}</span>{'>'}<span className={styles.codeString}>{'dbvisitor'}</span>{'</'}<span className={styles.codeKeyword}>{'artifactId'}</span>{'>\n'}
                            {'  <'}<span className={styles.codeKeyword}>{'version'}</span>{'>'}<span className={styles.codeString}>{Vars.lastReleaseVer}</span>{'</'}<span className={styles.codeKeyword}>{'version'}</span>{'>\n'}
                            {'</'}<span className={styles.codeKeyword}>{'dependency'}</span>{'>\n'}
                        </pre>
                    </div>
                    <div className={styles.quickStartButtons}>
                        <Link className="button button--primary button--lg" to="/docs/guides/overview">
                            <Translate id="commons.document">使用手册</Translate>
                        </Link>
                        <Link className="button button--outline button--primary button--lg" to="/docs/guides/search">
                            <Translate id="commons.search">快速检索</Translate>
                        </Link>
                    </div>
                </div>
            </div>
        </section>
    );
}

/* ==================== Bottom CTA Section ==================== */

function CtaSection() {
    return (
        <section className={styles.ctaSection}>
            <div className="container">
                <h2 className={styles.ctaTitle}>
                    <Translate id="dbv.cta.title">加入社区</Translate>
                </h2>
                <p className={styles.ctaDesc}>
                    <Translate id="dbv.cta.desc">
                        Apache 2.0 开源协议 · 核心依赖 Cobble · 6.7.1 起要求 Java 17+
                    </Translate>
                </p>
                <div className={styles.ctaLinks}>
                    {LabelList.map((item, idx) => (
                        <a key={idx} target="_blank" rel="noopener noreferrer" href={item.href}>
                            <item.Svg alt={item.alt}/>
                        </a>
                    ))}
                </div>
            </div>
        </section>
    );
}

/* ==================== Page Entry ==================== */

export default function Home() {
    return (
        <Layout>
            <HeroSection/>
            <main>
                <DatabaseSection/>
                <PhilosophySection/>
                <ArchitectureSection/>
                <ApiStylesSection/>
                <CodeExampleSection/>
                <QuickStartSection/>
            </main>
            <CtaSection/>
        </Layout>
    );
}
