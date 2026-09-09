import React from 'react';
import Translate from '@docusaurus/Translate';

export default function TagYellow() {
    let styleObj = {
        backgroundColor: 'rgb(255, 186, 0)', borderRadius: '2px', color: '#fff', padding: '0.2rem', fontSize: '75%'
    }
    return (<span style={styleObj}><Translate id="commons.optional">可选</Translate></span>);
}
