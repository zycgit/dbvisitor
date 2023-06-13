import React from 'react';
import clsx from 'clsx';
import Layout from '@theme/Layout';
import Link from '@docusaurus/Link';
import Translate from '@docusaurus/Translate';
import useDocusaurusContext from '@docusaurus/useDocusaurusContext';
import styles from './index.module.css';
import FakerFeatures from '../../components/fakerFeatures';
import FakerDataSources from '../../components/fakerDataSources';

function HomepageHeader() {
    const {siteConfig} = useDocusaurusContext();
    return (
        <header className={clsx('hero hero--primary', styles.heroBanner)}>
            <div className="container">
                <h1 className="hero__title">Faker</h1>
                <p className="hero__subtitle"><Translate id="homepage.tagline">全能型数据生成工具/数据库性能测试工具</Translate>
                    <br/><br/>
                </p>
                <div className={styles.buttons}>
                    <Link className="button button--secondary button--lg" to="/docs/faker/quickstart"><Translate id="commons.quick_start">快速上手</Translate></Link>
                    <div style={{width: 20}}/>
                    <Link className="button button--secondary button--lg" to="/docs/faker/overview"><Translate id="commons.document">使用手册</Translate></Link>
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
                <FakerDataSources/>
                <FakerFeatures/>
            </main>
        </Layout>
    );
}
