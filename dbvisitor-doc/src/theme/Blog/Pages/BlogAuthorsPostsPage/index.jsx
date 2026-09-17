import React from 'react';
import {useHistory} from '@docusaurus/router';
import {translate} from '@docusaurus/Translate';
import {HtmlClassNameProvider, PageMetadata, ThemeClassNames} from '@docusaurus/theme-common';
import {useBlogAuthorPageTitle, BlogAuthorNoPostsLabel} from '@docusaurus/theme-common/internal';
import {useBlogMetadata} from '@docusaurus/plugin-content-blog/client';
import Layout from '@theme/Layout';
import SearchMetadata from '@theme/SearchMetadata';
import BlogListPaginator from '@theme/BlogListPaginator';
import AuthorSocials from '@theme/Blog/Components/Author/Socials';
import BlogCollectionPage from '@site/src/components/BlogCollectionPage';

export default function BlogAuthorsPostsPage({author, items, listMetadata}) {
    const title = useBlogAuthorPageTitle(author);
    const {authorsListPath} = useBlogMetadata();
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
        <HtmlClassNameProvider className={`${ThemeClassNames.wrapper.blogPages} ${ThemeClassNames.page.blogAuthorsPostsPage}`}>
            <PageMetadata title={title} description={author.description} />
            <SearchMetadata tag="blog_authors_posts" />
            <Layout>
                <BlogCollectionPage title={author.name} count={listMetadata.totalCount}
                    description={author.description} imageURL={author.imageURL} posts={posts}
                    details={<>{author.title && <span>{author.title}</span>}<AuthorSocials author={author} /></>}
                    backTo={authorsListPath} onBack={goBack}
                    backLabel={translate({id: 'blog.article.back', message: '返回'})}
                    emptyMessage={<p><BlogAuthorNoPostsLabel /></p>}>
                    {(listMetadata.previousPage || listMetadata.nextPage) && <BlogListPaginator metadata={listMetadata} />}
                </BlogCollectionPage>
            </Layout>
        </HtmlClassNameProvider>
    );
}
