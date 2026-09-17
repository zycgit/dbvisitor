import React from 'react';
import Link from '@docusaurus/Link';
import useDocusaurusContext from '@docusaurus/useDocusaurusContext';
import {usePluginData} from '@docusaurus/useGlobalData';
import {HtmlClassNameProvider, PageMetadata, ThemeClassNames} from '@docusaurus/theme-common';
import Layout from '@theme/Layout';
import Heading from '@theme/Heading';
import BlogBrowseLinks from '@site/src/components/BlogBrowseLinks';
import BlogTagBrowser from '@site/src/components/BlogTagBrowser';
import styles from './styles.module.css';

function ArticleList({posts}) {
    return (
        <ul className={styles.articles}>
            {posts.map(post => <li key={post.permalink}><Link to={post.permalink} title={post.title}>{post.title}</Link></li>)}
        </ul>
    );
}

export default function BlogTagsListPage() {
    const {i18n: {currentLocale}} = useDocusaurusContext();
    const language = currentLocale === 'en' ? 'en' : 'zh';
    const text = value => value[language];
    const {topics} = usePluginData('blog-topics');
    const title = text({zh: '专栏', en: 'Columns'});

    return (
        <HtmlClassNameProvider className={`${ThemeClassNames.wrapper.blogPages} ${ThemeClassNames.page.blogTagsListPage}`}>
            <PageMetadata title={title} />
            <Layout>
                <main className={styles.page} aria-label={title}>
                    <BlogBrowseLinks />
                    <div className={styles.topics}>
                        {topics.map(topic => (
                            <section key={topic.id} className={styles.topic} aria-labelledby={topic.id}>
                                <Heading as="h2" id={topic.id}><Link to={topic.permalink}>{topic.title}</Link></Heading>
                                {topic.description && <p className={styles.description}>{topic.description}</p>}
                                <ArticleList posts={topic.posts} />
                                <div className={styles.cardFooter}>
                                    <span>{text({zh: `${topic.count} 篇文章`, en: `${topic.count} articles`})}</span>
                                    <Link to={topic.permalink} aria-label={text({zh: `查看全部：${topic.title}`, en: `View all: ${topic.title}`})}>
                                        {text({zh: '查看全部', en: 'View all'})}
                                    </Link>
                                </div>
                            </section>
                        ))}
                    </div>
                    <BlogTagBrowser />
                </main>
            </Layout>
        </HtmlClassNameProvider>
    );
}
