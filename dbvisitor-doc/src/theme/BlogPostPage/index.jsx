import React from 'react';
import BlogPostPage from '@theme-original/BlogPostPage';

export default function ArticlePage(props) {
    return <BlogPostPage {...props} sidebar={{...props.sidebar, items: []}} />;
}
