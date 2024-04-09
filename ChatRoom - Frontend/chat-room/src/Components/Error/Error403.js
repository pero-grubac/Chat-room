import Error from "./Error";
const Error403 = () => {
  return (
    <Error
      status="403"
      title="403"
      subTitle="Not Acceptable: The server cannot produce a response that is acceptable according to the request's Accept headers."
    />
  );
};
export default Error403;
