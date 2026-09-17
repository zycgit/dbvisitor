import React from 'react';
import useBaseUrl from '@docusaurus/useBaseUrl';
import useDocusaurusContext from '@docusaurus/useDocusaurusContext';
import {HtmlClassNameProvider, PageMetadata, ThemeClassNames} from '@docusaurus/theme-common';
import Layout from '@theme/Layout';
import BlogCollectionPage from '@site/src/components/BlogCollectionPage';

export default function BlogTopicPage({topic}) {
    const {i18n: {currentLocale}} = useDocusaurusContext();
    const language = currentLocale === 'en' ? 'en' : 'zh';
    const text = value => value[language];
    const overviewUrl = useBaseUrl('/blog/tags');
    return (
        <HtmlClassNameProvider className={ThemeClassNames.wrapper.blogPages}>
            <PageMetadata title={topic.title} description={topic.description} />
            <Layout>
                <BlogCollectionPage {...topic} backTo={overviewUrl}
                    backLabel={text({zh: '返回全部专栏', en: 'Back to all columns'})} />
            </Layout>
        </HtmlClassNameProvider>
    );
}
