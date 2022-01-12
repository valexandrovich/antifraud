import React, { Component } from 'react';
import { Link } from 'react-router-dom';
import ForgotPassword from './ForgotPassword';

class Login extends Component {
    state = {}

    loginHandler = () => {
        this.props.history.push('/dashboard');
        window.location.reload();
    }

    render() {
        return (
            <>
                <div className="fullsheet">
                    <div className="auth-form">

                        <h4 className="modal-title">Login to Your Account</h4>
                        <form>
                            <div className="form-group">
                                <input type="email"
                                    className="form-control"
                                    ref="email"
                                    name="email"
                                    placeholder="Enter Email"
                                    required
                                />
                                {/* <small className="text-danger">{this.state.email}</small> */}
                            </div>
                            <div className="form-group">
                                <input type="password"
                                    className="form-control"
                                    ref="password"
                                    name="password"
                                    placeholder="***********"
                                    required
                                />
                                {/* <small className="text-danger">{this.state.password}</small> */}
                            </div>
                            <div className="form-group small clearfix">
                                <label className="checkbox-inline"><input type="checkbox"/> Remember me</label>
                                <button type="button" data-toggle="modal" data-target="#model" className="forgot-link btn_modal">Forgot Password?</button>
                            </div>
                            <button onClick={this.loginHandler} className="btn btn-primary btn-block btn-lg form-control mb-3">Login</button>
                        </form>
                        <div className="text-center small text-dark">Don't have an account? <Link to='/singup'>Sign up</Link></div>
                    </div>
                </div>

                <div className="modal fade" id="model" role="dialog" aria-labelledby="model" aria-hidden="true">
                    <div className="modal-dialog modal-dialog-centered" role="document">
                        <ForgotPassword />
                    </div>
                </div>

            </>
        );
    }
}

export default Login;
