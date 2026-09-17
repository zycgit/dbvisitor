import React from 'react';
import useDocusaurusContext from '@docusaurus/useDocusaurusContext';
import {usePluginData} from '@docusaurus/useGlobalData';
import Heading from '@theme/Heading';
import Tag from '@theme/Tag';
import styles from './styles.module.css';

export default function BlogTagBrowser() {
    const {i18n: {currentLocale}} = useDocusaurusContext();
    const {tags} = usePluginData('blog-topics');
    if (!tags.length) return null;
    return (
        <section className={styles.tags} aria-labelledby="browse-by-tag">
            <Heading as="h2" id="browse-by-tag">{currentLocale === 'en' ? 'Browse by tag' : '按标签查找'}</Heading>
            <ul>
                {[...tags].sort((left, right) => left.label.localeCompare(right.label, currentLocale)).map(tag => (
                    <li key={tag.permalink}><Tag {...tag} /></li>
                ))}
            </ul>
        </section>
    );
}
