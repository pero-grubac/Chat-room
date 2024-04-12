import Error from "./Error";
const Error406 = () => {
  return (
    <Error
      status="404"
      title="406"
      subTitle="Not Acceptable: The server cannot produce a response that is acceptable according to the request's Accept headers."
    />
  );
};
export default Error406;
