import React from 'react';
import BlogSidebarContent from '@theme-original/BlogSidebar/Content';
import BlogBrowseLinks from '@site/src/components/BlogBrowseLinks';

// The mobile sidebar uses this shared content inside its navigation drawer.
export default function BlogSidebarContentWithLinks(props) {
    return (
        <>
            <BlogBrowseLinks />
            <BlogSidebarContent {...props} />
        </>
    );
}
