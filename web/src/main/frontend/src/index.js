import React from "react";
import ReactDOM from "react-dom";
import App from "./App";
import * as serviceWorker from "./serviceWorker";
import {HashRouter} from "react-router-dom";
import {Provider} from "react-redux";
import store from "./store/store";

ReactDOM.render(
    <Provider store={store}>
        <HashRouter>
            <App/>
        </HashRouter>
    </Provider>,
    document.getElementById("root")
);

serviceWorker.unregister();
