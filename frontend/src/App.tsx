import CreateCustomerPage from "./app/customers/new/page";
import { Link, Route, Routes } from "react-router-dom";
import LoginPage from "./app/auth/login/page";
import RegisterPage from "./app/auth/register/page";
import CustomerDetailPage from "./app/customers/[customerId]/page";
import EditCustomerPage from "./app/customers/[customerId]/edit/page";

function HomePage() {
  return (
    <main>
      <h1>BankApp</h1>
      <p>Identity and customer management frontend baseline.</p>
      <p>
        <Link to="/auth/register">Create an account</Link>
      </p>
      <p>
        <Link to="/auth/login">Sign in</Link>
      </p>
      <p>
        <Link to="/customers/new">Create customer</Link>
      </p>
      <p>
        <Link to="/customers/1">View sample customer</Link>
      </p>
    </main>
  );
}

export default function App() {
  return (
    <Routes>
      <Route path="/" element={<HomePage />} />
      <Route path="/auth/register" element={<RegisterPage />} />
      <Route path="/auth/login" element={<LoginPage />} />
      <Route path="/customers/new" element={<CreateCustomerPage />} />
      <Route path="/customers/:customerId" element={<CustomerDetailPage />} />
      <Route path="/customers/:customerId/edit" element={<EditCustomerPage />} />
    </Routes>
  );
}

