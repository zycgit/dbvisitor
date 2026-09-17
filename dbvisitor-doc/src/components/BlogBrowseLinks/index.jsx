import React from 'react';
import Link from '@docusaurus/Link';
import Translate, {translate} from '@docusaurus/Translate';
import useBaseUrl from '@docusaurus/useBaseUrl';
import {useLocation} from '@docusaurus/router';
import styles from './styles.module.css';

export default function BlogBrowseLinks() {
    const latestUrl = useBaseUrl('/blog');
    const archiveUrl = useBaseUrl('/blog/archive');
    const tagsUrl = useBaseUrl('/blog/tags');
    const topicsUrl = useBaseUrl('/blog/topics');
    const {pathname} = useLocation();
    const path = pathname.replace(/\/$/, '');
    const isArchive = path === archiveUrl;
    const isTopic = path === tagsUrl || path.startsWith(tagsUrl + '/') || path.startsWith(topicsUrl + '/');
    return (
        <div className={styles.links} role="group"
             aria-label={translate({id: 'blog.browse.navigation', message: '博客导航'})}>
            <Link to={archiveUrl} aria-current={isArchive ? 'page' : undefined}>
                <Translate id="blog.browse.archive">全部</Translate>
            </Link>
            <Link to={tagsUrl} aria-current={isTopic ? 'page' : undefined}>
                <Translate id="blog.browse.tags">专栏</Translate>
            </Link>
            <Link to={latestUrl} aria-current={!isArchive && !isTopic ? 'page' : undefined}>
                <Translate id="blog.browse.latest">最新</Translate>
            </Link>
        </div>
    );
}
