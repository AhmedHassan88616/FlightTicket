package com.example.flightticket.ui;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.flightticket.network.ApiClient;
import com.example.flightticket.network.ApiService;
import com.example.flightticket.network.model.Price;
import com.example.flightticket.network.model.Ticket;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Function;
import io.reactivex.observables.ConnectableObservable;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;

public class TicketViewModel extends ViewModel {

    private static final String from = "DEL";
    private static final String to = "HYD";

    MutableLiveData<List<Ticket>> ticketListMutableLiveData = new MutableLiveData<>();
    MutableLiveData<Ticket> ticketWithPriceMutableLiveData = new MutableLiveData<>();

    List<Ticket> ticketList = new ArrayList<>();

    ApiService apiService = ApiClient.getClient().create(ApiService.class);


    public void getTickets() {
        ConnectableObservable<List<Ticket>> ticketsObservable = getTicketsObservable(from, to).replay();


        /**
         * Fetching all tickets first
         * Observable emits List<Ticket> at once
         * All the items will be added to RecyclerView
         * */

        ticketsObservable
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new Observer<List<Ticket>>() {


                    @Override
                    public void onSubscribe(Disposable d) {
                    }

                    @Override
                    public void onNext(List<Ticket> tickets) {
                        ticketListMutableLiveData.setValue(tickets);

                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onComplete() {

                    }
                });


        /**
         //         * Fetching individual ticket price
         //         * First FlatMap converts single List<Ticket> to multiple emissions
         //         * Second FlatMap makes HTTP call on each Ticket emission
         //         * */


        ticketsObservable
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                /**
                 * Converting List<Ticket> emission to single Ticket emissions
                 * */
                .concatMap(new Function<List<Ticket>, ObservableSource<Ticket>>() {
                    @Override
                    public ObservableSource<Ticket> apply(List<Ticket> tickets) throws Exception {
                        return Observable.fromIterable(tickets);
                    }
                })
                /**
                 * Fetching price on each Ticket emission
                 * */
                .flatMap(new Function<Ticket, ObservableSource<Ticket>>() {
                    @Override
                    public ObservableSource<Ticket> apply(Ticket ticket) throws Exception {
                        return getPriceObservable(ticket);
                    }
                }).subscribeWith(new Observer<Ticket>() {
            @Override
            public void onSubscribe(Disposable d) {

            }

            @Override
            public void onNext(Ticket ticket) {
                ticketWithPriceMutableLiveData.setValue(ticket);
            }

            @Override
            public void onError(Throwable e) {

            }

            @Override
            public void onComplete() {

            }
        });

        // Calling connect to start emission
        ticketsObservable.connect();
    }

    Observable<List<Ticket>> getTicketsObservable(String from, String to) {
        return apiService.searchTickets(from, to)
                .toObservable()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());

    }


    /**
     * Making Retrofit call to get single ticket price
     * get price HTTP call returns Price object, but
     * map() operator is used to change the return type to Ticket
     */


    private Observable<Ticket> getPriceObservable(Ticket ticket) {
        return apiService.getPrice(ticket.getFlightNumber(), ticket.getFrom(), ticket.getTo())
                .toObservable()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .map(new Function<Price, Ticket>() {
                    @Override
                    public Ticket apply(Price price) throws Exception {
                        ticket.setPrice(price);
                        return ticket;
                    }
                });
    }
}
