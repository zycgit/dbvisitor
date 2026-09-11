import React, {useEffect} from 'react';
import Head from '@docusaurus/Head';
import Layout from '@theme/Layout';

export default function DocumentRedirect({redirect}) {
    useEffect(() => {
        let anchor = window.location.hash.slice(1);
        try {
            anchor = decodeURIComponent(anchor);
        } catch {
            // Preserve malformed fragments as-is instead of breaking navigation.
        }
        const mapped = redirect.anchors[anchor];
        const target = new URL(mapped || redirect.target, window.location.origin);
        target.search = window.location.search;
        if (!mapped) {
            target.hash = window.location.hash;
        }
        window.location.replace(target.href);
    }, [redirect]);

    return (
        <Layout>
            <Head><meta name="robots" content="noindex" /></Head>
            <main className="container margin-vert--lg">
                <a href={redirect.target}>文档已迁移 / Continue to documentation</a>
            </main>
        </Layout>
    );
}
