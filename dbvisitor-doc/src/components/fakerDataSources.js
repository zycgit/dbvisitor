import React from 'react';
import clsx from 'clsx';
import styles from './fakerDataSources.module.css';
import Translate, {translate} from '@docusaurus/Translate';

const DsIconList = [
    {
        title: translate({id: 'homepage.feature1_title', message: 'MySQL'}),
        Svg: require('../../static/img/ds_icons/mysql.svg').default
    },
    {
        title: translate({id: 'homepage.feature1_title', message: 'PostgreSQL'}),
        Svg: require('../../static/img/ds_icons/postgresql.svg').default
    },
    {
        title: translate({id: 'homepage.feature1_title', message: 'SQL SERVER'}),
        Svg: require('../../static/img/ds_icons/sqlserver.svg').default
    },
    {
        title: translate({id: 'homepage.feature1_title', message: 'Oracle'}),
        Svg: require('../../static/img/ds_icons/oracle.svg').default
    },
//    {
//        title: translate({id: 'homepage.feature3_title', message: 'IBM Db2'}),
//        Svg: require('../../static/img/ds_icons/ibmdb2.svg').default
//    },
//    {
//        title: translate({id: 'homepage.feature1_title', message: 'ClickHouse'}),
//        Svg: require('../../static/img/ds_icons/clickhouse.svg').default
//    },
//    {
//        title: translate({id: 'homepage.feature1_title', message: 'OceanBase'}),
//        Svg: require('../../static/img/ds_icons/oceanbase.svg').default
//    },
//    {
//        title: translate({id: 'homepage.feature2_title', message: 'Doris'}),
//        Svg: require('../../static/img/ds_icons/doris.svg').default
//    },
//    {
//        title: translate({id: 'homepage.feature3_title', message: 'StarRocks'}),
//        Svg: require('../../static/img/ds_icons/starrocks.svg').default
//    },
//    {
//        title: translate({id: 'homepage.feature3_title', message: 'TiDB'}),
//        Svg: require('../../static/img/ds_icons/tidb.svg').default
//    },
];

function DsIcon({Svg, title, description}) {
    return (
        <div className="">
            <div className="text--center">
                <Svg className={styles.featureSvg} alt={title}/>
            </div>
            <div className="text--center padding-horiz--md">
                <h6>{title}</h6>
            </div>
        </div>
    );
}

export default function HomepageFeatures() {
    return (
        <section className={styles.features}>
            <div className="container">
                <div className="text--center"><h3><Translate id="faker.datasource_title">已支持 全类型 或 极端值 的数据源</Translate></h3></div>
                <div className={styles.featureBlock}>
                    {DsIconList.map((props, idx) => (
                        <DsIcon key={idx} {...props} />
                    ))}
                </div>
            </div>
        </section>
    );
}
