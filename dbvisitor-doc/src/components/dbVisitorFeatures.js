import React from 'react';
import clsx from 'clsx';
import styles from './dbVisitorFeatures.module.css';
import Translate, {translate} from '@docusaurus/Translate';

const FeatureList = [
    {
        title: translate({id: 'dbv.feature1_title', message: '功能完备'}),
        Svg: require('../../static/img/dbv_full.svg').default,
        description: (
            <><Translate id="dbv.feature1_desc">对象映射、类型处理、动态SQL、存储过程、分页查询、嵌套事务、事务隔离级别、多数据源</Translate></>
        ),
    },
    {
        title: translate({id: 'dbv.feature2_title', message: '熟悉的方式'}),
        Svg: require('../../static/img/dbv_nice.svg').default,
        description: (
            <><Translate id="dbv.feature2_desc">兼容 Spring JdbcTemplate、兼容 MyBatis 映射文件、单表 ActiveRecord 方式</Translate></>
        ),
    },
    {
        title: translate({id: 'dbv.feature3_title', message: '特色优势'}),
        Svg: require('../../static/img/dbv_self.svg').default,
        description: (
            <><Translate id="dbv.feature3_desc">独立使用、更加丰富的 TypeHandler、方言20+、条件构造器、INSERT 策略、多语句/多结果</Translate></>
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
