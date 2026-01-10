import React from 'react';
import clsx from 'clsx';
import Layout from '@theme/Layout';
import Link from '@docusaurus/Link';
import Translate from '@docusaurus/Translate';
import useDocusaurusContext from '@docusaurus/useDocusaurusContext';
import styles from './index.module.css';
import DbVisitorFeatures from '../components/dbVisitorFeatures';

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
                <h1 className="hero__title">dbVisitor</h1>
                <p className="hero__subtitle">
                    <Translate id="dbv.tagline">dbVisitor 提供了一种统一且简便的方式，可访问多种不同类型的数据库。</Translate>
                    <br/><br/>
                    {LabelList.map((props, idx) => (
                        <Label key={idx} {...props} />
                    ))}
                </p>
                <div className={styles.buttons}>
                    <Link className="button button--secondary button--lg" to="/docs/guides/overview"><Translate id="commons.document">使用手册</Translate></Link>
                    <div style={{width: 20}}/>
                    <Link className="button button--secondary button--lg" to="/docs/guides/search"><Translate id="commons.search">快速检索</Translate></Link>
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
                <DbVisitorFeatures/>
            </main>
        </Layout>
    );
}
