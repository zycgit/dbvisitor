import React from 'react';
import {useHistory} from '@docusaurus/router';
import {translate} from '@docusaurus/Translate';
import {HtmlClassNameProvider, PageMetadata, ThemeClassNames} from '@docusaurus/theme-common';
import {useBlogTagsPostsPageTitle} from '@docusaurus/theme-common/internal';
import Layout from '@theme/Layout';
import SearchMetadata from '@theme/SearchMetadata';
import BlogListPaginator from '@theme/BlogListPaginator';
import Unlisted from '@theme/ContentVisibility/Unlisted';
import BlogCollectionPage from '@site/src/components/BlogCollectionPage';

export default function BlogTagsPostsPage({tag, items, listMetadata}) {
    const title = useBlogTagsPostsPageTitle(tag);
    const history = useHistory();
    const posts = items.map(({content}) => content.metadata);
    function goBack(event) {
        if (event.button !== 0 || event.metaKey || event.ctrlKey || event.shiftKey || event.altKey) return;
        if (window.history.length > 1) {
            event.preventDefault();
            history.goBack();
        }
    }
    return (
        <HtmlClassNameProvider className={`${ThemeClassNames.wrapper.blogPages} ${ThemeClassNames.page.blogTagPostListPage}`}>
            <PageMetadata title={title} description={tag.description} />
            <SearchMetadata tag="blog_tags_posts" />
            <Layout>
                <BlogCollectionPage title={tag.label} count={listMetadata.totalCount}
                    description={tag.description} posts={posts}
                    notice={tag.unlisted && <Unlisted />}
                    backTo={tag.allTagsPath} onBack={goBack}
                    backLabel={translate({id: 'blog.article.back', message: '返回'})}>
                    {(listMetadata.previousPage || listMetadata.nextPage) && <BlogListPaginator metadata={listMetadata} />}
                </BlogCollectionPage>
            </Layout>
        </HtmlClassNameProvider>
    );
}
