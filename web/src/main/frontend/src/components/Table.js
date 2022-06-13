import React from "react";

const Table = (props) => {
    const getKeys = () => {
        return Object.keys(props.data[0]);
    };

    const getHeader = () => {
        let keys = getKeys();
        return keys.map((key, index) => {
            return <th key={index}>{key.toUpperCase()}</th>;
        });
    };

    const getRowsData = () => {
        const keys = getKeys();
        return props.data.map((row, index) => {
            const errorRow = props.err?.filter((el) => el.personId === row.id);
            return (
                <tr key={row.id}>
                    <RenderRow key={index} data={row} keys={keys} err={errorRow} eTag={props.errTag} id={row.id}
                               updateErrors={props.updateErrors}/>
                </tr>
            );
        });
    };

    return (
        <div className="sroll-x">
            <table className="table table-striped table-bordered table-sm">
                <thead>
                <tr>{getHeader()}</tr>
                </thead>
                <tbody>{getRowsData()}</tbody>
            </table>
        </div>
    );
};

export default Table;

const RenderRow = (props) => {
    return props.keys.map((key, index) => {
        const err = props.err?.find((e) => e.columnIndex + 2 === index);
        return err ? (
            <ErrCell updateErrors={props.updateErrors} idx={index - 2} key={index} data={props.data[key]} mis={err}
                     id={props.id}/>
        ) : (
            <td key={index}>{typeof props.data[key] === "object" && props.data[key] !== null ?
                <Tag updateErrors={props.updateErrors} errTag={props.eTag}
                     data={props.data[key]}/> : props.data[key]}</td>
        );
    });
};

const ErrCell = (props) => {
    const [edit, setEdit] = React.useState(false);
    const [errValue, setErrValue] = React.useState(props.data);
    const {name, id, idx} = props;

    return (
        <>
            {edit ? (
                <div>
                    <div
                        style={{position: "relative", width: "200px"}}
                        className="form-group"
                    >
                        <label className="text-danger" htmlFor="errorCell">
                            {props.mis.message}
                        </label>
                        <input
                            name="errorCell"
                            type="text"
                            className="form-control"
                            value={errValue}
                            onChange={(e) => setErrValue(e.target.value)}
                        />
                    </div>
                    <div className="d-flex justify-content-between mt-1">
                        <button onClick={() => props.updateErrors(id, idx, errValue)} type="button"
                                className="btn custom-btn  btn-default">
                            Змінити
                        </button>
                        <button onClick={() => setEdit(!edit)} type="button" className="btn btn-danger btn-default">
                            Закрити
                        </button>
                    </div>
                </div>
            ) : (
                <td onClick={() => setEdit(true)} className="d-flex flex-column align-items-center text-danger">
                    <span className="table-header">{name?.replace("mk", "")}</span><span>{props.data} </span>
                </td>
            )}
        </>
    );
};

const Tag = ({data, errTag, updateErrors}) => {
    return (
        <>
            {
                data.map((el) => {
                    const err = errTag?.filter((e) => e.tagId === el.id);
                    return (
                        <div key={el.id} className="tags d-flex border-bottom">
                            {Object.entries(el).slice(1).map(([k, v], index) => {
                                    const errCell = err?.find((e) => e.columnIndex === index);
                                    if (errCell?.columnIndex === index) {
                                        return (<>{<ErrCell updateErrors={updateErrors} key={index} idx={index} id={el.id}
                                                            data={v} mis={errCell.columnIndex === index && errCell}
                                                            name={k}/>}</>);
                                    }
                                    return (<div className="d-flex flex-column align-items-center" key={index}><span
                                        className="table-header">{k.replace("mk", "")}</span><span>{v} </span></div>);
                                }
                            )}
                        </div>
                    );
                })
            }
        </>
    );
};


