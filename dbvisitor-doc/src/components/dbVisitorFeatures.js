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
                             redis: <Link to="/docs/guides/drivers/redis/about">Redis</Link>,
                         }}>
                {"支持关系型数据库如 MySQL、Oracle、PostgreSQL 等，支持非关系型数据库如 {redis}"}
            </Translate>
            </>
        ),
    },
    {
        title: translate({id: 'dbv.feature1_title', message: '简单且完备'}),
        Svg: require('../../static/img/dbv_full.svg').default,
        description: (
            <><Translate id="dbv.feature1_desc">对象映射、类型处理、动态SQL、存储过程、分页查询、嵌套事务、事务隔离级别、多数据源</Translate></>
        ),
    },
    {
        title: translate({id: 'dbv.feature3_title', message: '特色优势'}),
        Svg: require('../../static/img/dbv_self.svg').default,
        description: (
            <><Translate id="dbv.feature3_desc">最直接的、没有认知负担、不隐藏 SQL、没有魔法、兼容 MyBatis</Translate></>
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
