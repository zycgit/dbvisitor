import React from 'react';
import clsx from 'clsx';
import Layout from '@theme/Layout';
import Link from '@docusaurus/Link';
import Translate from '@docusaurus/Translate';
import useDocusaurusContext from '@docusaurus/useDocusaurusContext';
import styles from './index.module.css';
import DbVisitorFeatures from '../components/dbVisitorFeatures';

function HomepageHeader() {
    const {siteConfig} = useDocusaurusContext();
    return (
        <header className={clsx('hero hero--primary', styles.heroBanner)}>
            <div className="container">
                <h1 className="hero__title">dbVisitor</h1>
                <p className="hero__subtitle">
                    <Translate id="dbv.tagline">dbVisitor 提供了一种统一且简便的方式，可访问多种不同类型的数据库。</Translate>
                    <br/><br/>
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
