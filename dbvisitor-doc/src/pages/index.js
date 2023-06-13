import React from 'react';
import clsx from 'clsx';
import Layout from '@theme/Layout';
import Link from '@docusaurus/Link';
import Translate from '@docusaurus/Translate';
import useDocusaurusContext from '@docusaurus/useDocusaurusContext';
import styles from './index.module.css';
import HomepageFeatures from '../components/homepageFeatures';

const LabelList = [
    {
        alt: 'License',
        Svg: require('../../static/img/labels/license.svg').default,
        href: 'https://www.apache.org/licenses/LICENSE-2.0.html'
    },
    {
        alt: 'Maven',
        Svg: require('../../static/img/labels/license.svg').default,
        href: 'https://maven-badges.herokuapp.com/maven-central/net.hasor/dbvisitor'
    },
    {
        alt: 'License',
        Svg: require('../../static/img/labels/email-zyc@byshell.svg').default,
        href: 'mailto:zyc@byshell.org'
    },
    {
        alt: '技术交流群',
        Svg: require('../../static/img/labels/qqgroup-948706820.svg').default,
        href: 'https://qm.qq.com/cgi-bin/qm/qr?k=Qy3574A4VgI0ph4fqFbZW-w49gnyqu6p&jump_from=webapi'
    },
];

function Label({Svg, href, alt}) {
    return (
        <a className="button-padding" target="_blank" href={href}><Svg alt={alt}/></a>
    );
}

function HomepageHeader() {
    const {siteConfig} = useDocusaurusContext();
    return (
        <header className={clsx('hero hero--primary', styles.heroBanner)}>
            <div className="container">
                <h1 className="hero__title">dbVisitor & Faker</h1>
                <p className="hero__subtitle"><Translate id="homepage.tagline">轻量小巧的数据库开发工具</Translate>
                    <br/><br/>
                    {LabelList.map((props, idx) => (
                        <Label key={idx} {...props} />
                    ))}
                </p>
                <div className={styles.buttons}>
                    <Link className="button button--secondary button--lg" to="/dbvisitor">dbVisitor (ORM 框架)</Link>
                    <div style={{width: 20}}/>
                    <Link className="button button--secondary button--lg" to="/faker">Faker (数据生成器)</Link>
                </div>
            </div>
        </header>
    );
}

export default function Home() {
    return (
        <Layout>
            <HomepageHeader/>
            <main>
                <HomepageFeatures/>
            </main>
        </Layout>
    );
}
