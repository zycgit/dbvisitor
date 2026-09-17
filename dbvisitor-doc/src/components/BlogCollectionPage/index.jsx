import React from 'react';
import Link from '@docusaurus/Link';
import useBaseUrl from '@docusaurus/useBaseUrl';
import useDocusaurusContext from '@docusaurus/useDocusaurusContext';
import Heading from '@theme/Heading';
import IconArrow from '@theme/Icon/Arrow';
import styles from './styles.module.css';

// Author, tag and column indexes share a summary header and a flat article list.
export default function BlogCollectionPage({title, count, description, imageURL, details,
    posts, backTo, backLabel, onBack, emptyMessage, notice, children}) {
    const {i18n: {currentLocale}} = useDocusaurusContext();
    const english = currentLocale === 'en';
    const image = useBaseUrl(imageURL || '');
    return (
        <main className={styles.page}>
            {notice}
            <header className={styles.header}>
                <div className={styles.headerTop}>
                    <div className={styles.identity}>
                        {imageURL && <img className={styles.avatar} src={image} alt="" width="48" height="48" />}
                        <div className={styles.summary}>
                            <div className={styles.titleRow}>
                                <Heading as="h1">{title}</Heading>
                                <span className={styles.articleCount}>{english ? `${count} articles` : `${count} 篇文章`}</span>
                            </div>
                            {details && <div className={styles.details}>{details}</div>}
                            {description && <p>{description}</p>}
                        </div>
                    </div>
                    <Link to={backTo} onClick={onBack} className={styles.back} aria-label={backLabel} title={backLabel}>
                        <IconArrow className={styles.backIcon} viewBox="0 0 20 20" aria-hidden="true" />
                        {english ? 'Back' : '返回'}
                    </Link>
                </div>
            </header>
            {posts.length ? (
                <ul className={styles.articles}>
                    {posts.map(post => (
                        <li key={post.permalink}>
                            <Link to={post.permalink}>{post.title}</Link>
                            {post.description && <p>{post.description}</p>}
                        </li>
                    ))}
                </ul>
            ) : emptyMessage}
            {children}
        </main>
    );
}
