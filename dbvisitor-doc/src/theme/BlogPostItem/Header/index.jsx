import React from 'react';
import Link from '@docusaurus/Link';
import useBaseUrl from '@docusaurus/useBaseUrl';
import {useHistory} from '@docusaurus/router';
import Translate, {translate} from '@docusaurus/Translate';
import {useBlogPost} from '@docusaurus/plugin-content-blog/client';
import BlogPostItemHeaderTitle from '@theme/BlogPostItem/Header/Title';
import Tag from '@theme/Tag';
import ArticleInfo from './ArticleInfo';
import styles from './styles.module.css';

function CompactAuthor({author, imageURL}) {
    const image = useBaseUrl(imageURL || '');
    const name = author.name || translate({id: 'blog.article.author', message: '作者'});
    const url = author.page?.permalink || author.url || (author.email && `mailto:${author.email}`);
    const content = <>
        {imageURL && <img src={image} alt="" width="28" height="28" />}
        <span translate="no">{name}</span>
    </>;
    return url
        ? <Link to={url} className={styles.author}>{content}</Link>
        : <span className={styles.author}>{content}</span>;
}

export default function ArticleHeader() {
    const {isBlogPostPage, metadata: {authors, tags}, assets} = useBlogPost();
    const history = useHistory();
    const fallbackUrl = useBaseUrl('/blog/archive');
    function goBack(event) {
        if (event.button !== 0 || event.metaKey || event.ctrlKey || event.shiftKey || event.altKey) {
            return;
        }
        if (window.history.length > 1) {
            event.preventDefault();
            history.goBack();
        }
    }
    return (
        <>
            {isBlogPostPage && (
                <div className={styles.navigation}>
                    <Link to={fallbackUrl} onClick={goBack}>
                        <span aria-hidden="true">←</span>
                        <Translate id="blog.article.back">返回</Translate>
                    </Link>
                    {authors.length > 0 && (
                        <div className={styles.authors}>
                            {authors.map((author, index) => (
                                <CompactAuthor key={index} author={author}
                                    imageURL={assets.authorsImageUrls[index] ?? author.imageURL} />
                            ))}
                        </div>
                    )}
                </div>
            )}
            {isBlogPostPage ? (
                <header className={styles.header}>
                    <BlogPostItemHeaderTitle className={styles.title} />
                    <ArticleInfo className={styles.info} />
                </header>
            ) : (
                <header className={`${styles.header} ${styles.listHeader}`}>
                    <BlogPostItemHeaderTitle className={`${styles.title} ${styles.listTitle}`} />
                    {authors.length > 0 && (
                        <div className={styles.listAuthors}>
                            {authors.map((author, index) => (
                                <CompactAuthor key={index} author={author}
                                    imageURL={assets.authorsImageUrls[index] ?? author.imageURL} />
                            ))}
                        </div>
                    )}
                    <div className={styles.listMetadata}>
                        <ArticleInfo className={styles.listInfo} inline />
                        {tags.length > 0 && (
                            <ul className={styles.listTags} aria-label={translate({
                                id: 'theme.tags.tagsListLabel', message: 'Tags:',
                            })}>
                                {tags.map(tag => <li key={tag.permalink}><Tag {...tag} /></li>)}
                            </ul>
                        )}
                    </div>
                </header>
            )}
        </>
    );
}
