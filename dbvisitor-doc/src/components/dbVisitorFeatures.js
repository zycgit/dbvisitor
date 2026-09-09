import React from 'react';
import clsx from 'clsx';
import styles from './dbVisitorFeatures.module.css';
import Translate, {translate} from '@docusaurus/Translate';
import Link from '@docusaurus/Link';

const FeatureList = [
    {
        title: translate({id: 'dbv.feature2_title', message: '多数据源'}),
        Svg: require('../../static/img/dbv_nice.svg').default,
        description: (
            <><Translate id="dbv.feature2_desc"
                         values={{
                             redis: <Link to="/docs/features/redis/about">Redis</Link>,
                             mongo: <Link to="/docs/features/mongo/about">MongoDB</Link>,
                             elastic: <Link to="/docs/features/elastic/about">ElasticSearch</Link>,
                         }}>
                {"支持关系型数据库如 MySQL、Oracle、PostgreSQL 等，支持非关系型数据库如 {redis}、{mongo}、{elastic} 等"}
            </Translate>
            </>
        ),
    },
    {
        title: translate({id: 'dbv.feature1_title', message: '简单且完备'}),
        Svg: require('../../static/img/dbv_full.svg').default,
        description: (
            <><Translate id="dbv.feature1_desc">对象映射、类型处理、动态 SQL 和分页查询；事务、存储过程等能力取决于数据库和驱动</Translate></>
        ),
    },
    {
        title: translate({id: 'dbv.feature3_title', message: '特色优势'}),
        Svg: require('../../static/img/dbv_self.svg').default,
        description: (
            <><Translate id="dbv.feature3_desc">保留 SQL 与原生命令，支持注解和 XML Mapper；JDBC 兼容范围以各驱动手册为准</Translate></>
        ),
    },
];

function Feature({Svg, title, description}) {
    return (
        <div className={clsx('col col--4')}>
            <div className="text--center">
                <Svg className={styles.featureSvg} alt={title}/>
            </div>
            <div className="text--center padding-horiz--md">
                <h3>{title}</h3>
                <p>{description}</p>
            </div>
        </div>
    );
}

export default function HomepageFeatures() {
    return (
        <section className={styles.features}>
            <div className="container">
                <div className="row">
                    {FeatureList.map((props, idx) => (
                        <Feature key={idx} {...props} />
                    ))}
                </div>
            </div>
        </section>
    );
}
