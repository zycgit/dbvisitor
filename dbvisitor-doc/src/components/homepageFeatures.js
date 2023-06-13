import React from 'react';
import clsx from 'clsx';
import styles from './dbVisitorFeatures.module.css';
import Translate, {translate} from '@docusaurus/Translate';

const FeatureList = [
    {
        title: translate({id: 'homepage.feature1_title', message: 'dbVisitor'}),
        Svg: require('../../static/img/homepage_data_points.svg').default,
        description: (
            <><Translate id="homepage.feature1_desc">轻量小巧、功能完备、多种方式使用</Translate></>
        ),
    },
    {
        title: translate({id: 'homepage.feature2_title', message: 'Faker'}),
        Svg: require('../../static/img/homepage_feat-sso.svg').default,
        description: (
            <><Translate id="homepage.feature3_desc">数据生成、数据库性能测试、全类型、极端值</Translate></>
        ),
    },
];

function Feature({Svg, title, description}) {
    return (
        <div className={clsx('col col--6')}>
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
