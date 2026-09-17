/**
 * Copyright (c) Facebook, Inc. and its affiliates.
 * Adapted from @docusaurus/theme-classic, licensed under the MIT license.
 */
import React from 'react';
import clsx from 'clsx';
import {translate} from '@docusaurus/Translate';
import {useVisibleBlogSidebarItems, BlogSidebarItemList} from '@docusaurus/plugin-content-blog/client';
import BlogSidebarContent from '@theme-original/BlogSidebar/Content';
import styles from './styles.module.css';
import BlogBrowseLinks from '@site/src/components/BlogBrowseLinks';

function ListComponent({items}) {
    return (
        <BlogSidebarItemList
            items={items}
            ulClassName={clsx(styles.sidebarItemList, 'clean-list')}
            liClassName={styles.sidebarItem}
            linkClassName={styles.sidebarItemLink}
            linkActiveClassName={styles.sidebarItemLinkActive}
        />
    );
}

export default function BlogSidebarDesktop({sidebar}) {
    const items = useVisibleBlogSidebarItems(sidebar.items);
    return (
        <aside className="col col--3">
            <nav
                className={styles.sidebar}
                aria-label={translate({id: 'blog.browse.navigation', message: '博客导航'})}>
                <BlogBrowseLinks />
                <div className={clsx(styles.sidebarContent, 'thin-scrollbar')}>
                    <BlogSidebarContent
                        items={items}
                        ListComponent={ListComponent}
                        yearGroupHeadingClassName={styles.yearGroupHeading}
                    />
                </div>
            </nav>
        </aside>
    );
}
