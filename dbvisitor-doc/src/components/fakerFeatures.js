import React from 'react';
import clsx from 'clsx';
import styles from './fakerFeatures.module.css';
import Translate, {translate} from '@docusaurus/Translate';

const FeatureList = [
    {
        title: translate({id: 'faker.feature1_title', message: '类型完整'}),
        Svg: require('../../static/img/faker_full_types.svg').default,
        description: (
            <><Translate id="faker.feature1_desc">常见类型、精度/近似数、地理信息、数组、二进制类型</Translate></>
        ),
    },
    {
        title: translate({id: 'faker.feature2_title', message: '随机性'}),
        Svg: require('../../static/img/faker_random.svg').default,
        description: (
            <><Translate id="faker.feature2_desc">均匀分布、正态分布、DML事件随机、事务/批大小随机、生成策略</Translate></>
        ),
    },
    {
        title: translate({id: 'faker.feature3_title', message: '方便实用'}),
        Svg: require('../../static/img/faker_easy.svg').default,
        description: (
            <><Translate id="faker.feature3_desc">高性能、CLI 开箱即用、DSL 自定义生成策略、支持表/列级配置</Translate></>
        ),
    },
    {
        title: translate({id: 'faker.feature4_title', message: '容易扩展'}),
        Svg: require('../../static/img/faker_ext.svg').default,
        description: (
            <><Translate id="faker.feature4_desc">提供编程接口、自动加载生成策略无需编码</Translate></>
        ),
    }
];

function Feature({Svg, title, description}) {
    return (
        <div className={clsx('col col--3')}>
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
                <div className="text--center"><h3><Translate id="faker.featureTools_title">工具特点</Translate></h3></div>
                <div className="row">
                    {FeatureList.map((props, idx) => (
                        <Feature key={idx} {...props} />
                    ))}
                </div>
            </div>
        </section>
    );
}
