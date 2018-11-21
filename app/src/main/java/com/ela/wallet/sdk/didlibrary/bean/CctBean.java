package com.ela.wallet.sdk.didlibrary.bean;

import java.util.List;

/**
 * Created by Administrator on 2018/11/20.
 * http cct request return data
 */

public class CctBean {

    /**
     * result : {"Transactions":[{"UTXOInputs":[{"address":"ESs1jakyQjxBvEgwqEGxtceastbPAR1UJ4","txid":"583ca6c3780b3ba880b446c7ce5427e538a82fc185e54749e61805a97dc3b222","index":0}],"CrossChainAsset":[{"amount":100000000,"address":"EgRJA3p9amexkWJMsYkSSn576n6oRteKEP"}],"Fee":20000,"Outputs":[{"amount":100010000,"address":"XKUh4GLhFJiqAMTF6HyWQrV9pK9HcGUdfJ"},{"amount":9899980000,"address":"ESs1jakyQjxBvEgwqEGxtceastbPAR1UJ4"}]}]}
     * status : 200
     */

    private ResultBean result;
    private int status;

    public ResultBean getResult() {
        return result;
    }

    public void setResult(ResultBean result) {
        this.result = result;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public static class ResultBean {
        private List<TransactionsBean> Transactions;

        public List<TransactionsBean> getTransactions() {
            return Transactions;
        }

        public void setTransactions(List<TransactionsBean> Transactions) {
            this.Transactions = Transactions;
        }

        public static class TransactionsBean {
            /**
             * UTXOInputs : [{"address":"ESs1jakyQjxBvEgwqEGxtceastbPAR1UJ4","txid":"583ca6c3780b3ba880b446c7ce5427e538a82fc185e54749e61805a97dc3b222","index":0}]
             * CrossChainAsset : [{"amount":100000000,"address":"EgRJA3p9amexkWJMsYkSSn576n6oRteKEP"}]
             * Fee : 20000
             * Outputs : [{"amount":100010000,"address":"XKUh4GLhFJiqAMTF6HyWQrV9pK9HcGUdfJ"},{"amount":9899980000,"address":"ESs1jakyQjxBvEgwqEGxtceastbPAR1UJ4"}]
             */

            private int Fee;
            private List<UTXOInputsBean> UTXOInputs;
            private List<CrossChainAssetBean> CrossChainAsset;
            private List<OutputsBean> Outputs;

            public int getFee() {
                return Fee;
            }

            public void setFee(int Fee) {
                this.Fee = Fee;
            }

            public List<UTXOInputsBean> getUTXOInputs() {
                return UTXOInputs;
            }

            public void setUTXOInputs(List<UTXOInputsBean> UTXOInputs) {
                this.UTXOInputs = UTXOInputs;
            }

            public List<CrossChainAssetBean> getCrossChainAsset() {
                return CrossChainAsset;
            }

            public void setCrossChainAsset(List<CrossChainAssetBean> CrossChainAsset) {
                this.CrossChainAsset = CrossChainAsset;
            }

            public List<OutputsBean> getOutputs() {
                return Outputs;
            }

            public void setOutputs(List<OutputsBean> Outputs) {
                this.Outputs = Outputs;
            }

            public static class UTXOInputsBean {
                /**
                 * address : ESs1jakyQjxBvEgwqEGxtceastbPAR1UJ4
                 * txid : 583ca6c3780b3ba880b446c7ce5427e538a82fc185e54749e61805a97dc3b222
                 * index : 0
                 */

                private String address;
                private String txid;
                private int index;

                public String getAddress() {
                    return address;
                }

                public void setAddress(String address) {
                    this.address = address;
                }

                public String getTxid() {
                    return txid;
                }

                public void setTxid(String txid) {
                    this.txid = txid;
                }

                public int getIndex() {
                    return index;
                }

                public void setIndex(int index) {
                    this.index = index;
                }
            }

            public static class CrossChainAssetBean {
                /**
                 * amount : 100000000
                 * address : EgRJA3p9amexkWJMsYkSSn576n6oRteKEP
                 */

                private int amount;
                private String address;

                public int getAmount() {
                    return amount;
                }

                public void setAmount(int amount) {
                    this.amount = amount;
                }

                public String getAddress() {
                    return address;
                }

                public void setAddress(String address) {
                    this.address = address;
                }
            }

            public static class OutputsBean {
                /**
                 * amount : 100010000
                 * address : XKUh4GLhFJiqAMTF6HyWQrV9pK9HcGUdfJ
                 */

                private int amount;
                private String address;

                public int getAmount() {
                    return amount;
                }

                public void setAmount(int amount) {
                    this.amount = amount;
                }

                public String getAddress() {
                    return address;
                }

                public void setAddress(String address) {
                    this.address = address;
                }
            }
        }
    }
}
