import React from 'react';
import Translate from '@docusaurus/Translate';

export default function TagRed() {
    let styleObj = {
        backgroundColor: 'rgb(227 17 108)', borderRadius: '2px', color: '#fff', padding: '0.2rem', fontSize: '75%'
    }
    return (<span style={styleObj}><Translate id="commons.required">必选</Translate></span>);
}
