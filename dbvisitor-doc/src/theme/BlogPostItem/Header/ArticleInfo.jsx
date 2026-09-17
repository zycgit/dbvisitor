import React from 'react';
import {translate} from '@docusaurus/Translate';
import {usePluralForm} from '@docusaurus/theme-common';
import useBlogDateFormat from '@site/src/components/useBlogDateFormat';
import {useBlogPost} from '@docusaurus/plugin-content-blog/client';

// Share date formatting and translated reading-time labels across blog views.
export default function ArticleInfo({className, inline = false}) {
    const {metadata: {date, readingTime, frontMatter}} = useBlogPost();
    // Only explicit editorial updates count, not formatting edits from git history.
    const updated = frontMatter.last_update?.date;
    const formatDate = useBlogDateFormat();
    const {selectMessage} = usePluralForm();
    const minutes = Math.ceil(readingTime);
    return (
        <div className={className}>
            <span>
                {updated && `${translate({id: 'blog.article.published', message: '发布：'})}`}
                <time dateTime={date}>{formatDate(date)}</time>
            </span>
            {updated && <>
                {inline && ' · '}
                <span>{translate({id: 'blog.article.updated', message: '更新：'})}
                    <time dateTime={new Date(updated).toISOString()} itemProp="dateModified">{formatDate(updated)}</time>
                </span>
            </>}
            {inline && typeof readingTime !== 'undefined' && ' · '}
            {typeof readingTime !== 'undefined' && (
                <span>{selectMessage(minutes, translate({
                    id: 'theme.blog.post.readingTime.plurals',
                    message: 'One min read|{readingTime} min read',
                }, {readingTime: minutes}))}</span>
            )}
        </div>
    );
}
