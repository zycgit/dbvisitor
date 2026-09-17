import React from 'react';
import Link from '@docusaurus/Link';
import {translate} from '@docusaurus/Translate';
import {HtmlClassNameProvider, PageMetadata, ThemeClassNames} from '@docusaurus/theme-common';
import useBlogDateFormat from '@site/src/components/useBlogDateFormat';
import Layout from '@theme/Layout';
import Heading from '@theme/Heading';
import BlogBrowseLinks from '@site/src/components/BlogBrowseLinks';
import BlogTagBrowser from '@site/src/components/BlogTagBrowser';
import styles from './styles.module.css';

export default function BlogArchivePage({archive}) {
    const title = translate({id: 'theme.blog.archive.title', message: 'Archive'});
    const formatDate = useBlogDateFormat(false);
    const blogPosts = [...archive.blogPosts].sort((left, right) =>
        Date.parse(right.metadata.date) - Date.parse(left.metadata.date));
    const years = new Map();
    blogPosts.forEach(post => {
        const year = post.metadata.date.slice(0, 4);
        if (!years.has(year)) {
            years.set(year, []);
        }
        years.get(year).push(post);
    });

    return (
        <HtmlClassNameProvider className={ThemeClassNames.wrapper.blogPages}>
            <PageMetadata title={title} />
            <Layout>
                <main className={styles.archive} aria-label={title}>
                    <BlogBrowseLinks />
                    <div className={styles.years}>
                        {Array.from(years, ([year, posts]) => (
                            <section key={year}>
                                <Heading as="h2" id={year}>{year}</Heading>
                                <ul>
                                    {posts.map(({metadata}) => (
                                        <li key={metadata.permalink}>
                                            <Link to={metadata.permalink}>
                                                {formatDate(metadata.date)} - {metadata.title}
                                            </Link>
                                        </li>
                                    ))}
                                </ul>
                            </section>
                        ))}
                    </div>
                    <BlogTagBrowser />
                </main>
            </Layout>
        </HtmlClassNameProvider>
    );
}
